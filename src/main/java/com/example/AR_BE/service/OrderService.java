package com.example.AR_BE.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.AR_BE.domain.*;
import com.example.AR_BE.domain.dto.OrderDTO;
import com.example.AR_BE.domain.dto.OrderDetailDTO;
import com.example.AR_BE.domain.dto.OrderItemDTO;
import com.example.AR_BE.domain.dto.OrderItemDetailDTO;
import com.example.AR_BE.domain.request.CreateOrderRequest;
import com.example.AR_BE.domain.request.OrderItemRequest;
import com.example.AR_BE.domain.response.ResultPaginationDTO;
import com.example.AR_BE.repository.OrderRepository;
import com.example.AR_BE.repository.ProductRepository;
import com.example.AR_BE.repository.UserRepository;
import com.example.AR_BE.utils.SecurityUtils;
import com.example.AR_BE.utils.constants.RoleEnum;
import com.example.AR_BE.utils.constants.StatusEnum;
import com.example.AR_BE.utils.exception.IdInvalidException;
import com.example.AR_BE.utils.pricing.PriceCalculator;
import com.example.AR_BE.validator.OrderValidator;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final UserRepository userRepository;
    private final ProductService productService;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final PriceCalculator priceCalculator;
    private final OrderValidator orderValidator;

    /**
     * Tạo order từ CreateOrderRequest.
     * - Validate input
     * - Kiểm tra stock
     * - Tính giá tại thời điểm mua (priceAtPurchase)
     * - Trừ stock
     * - Lưu order (cascade lưu orderItems)
     */

    @Transactional
    public Order createOrder(CreateOrderRequest req) {

        // 1. Lấy user hiện tại từ token
        String username = SecurityUtils.getCurrentUserLogin()
                .orElseThrow(() -> new RuntimeException("User not logged in"));
        User user = userRepository.findByEmail(username);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        // 2. Validate request
        orderValidator.validateRequest(req);

        // 3. Tạo order
        Order order = new Order();
        order.setUser(user); // user từ token
        order.setShippingAddress(req.getShippingAddress());
        order.setStatus(StatusEnum.PENDING);

        List<OrderItem> items = new ArrayList<>();
        double total = 0;

        // 4. Duyệt từng item
        for (OrderItemRequest itemReq : req.getItems()) {

            orderValidator.validateItem(itemReq);

            // Lấy product + validate stock
            Product product = productService.getAndValidateProduct(
                    itemReq.getProductId(),
                    itemReq.getQuantity());

            // Tính giá
            double price = priceCalculator.calculatePrice(product);

            // Tạo order item
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(itemReq.getQuantity());
            item.setPriceAtPurchase(price);
            item.setProductType(product.getCategory().getName());

            total += price * itemReq.getQuantity();
            items.add(item);

            // Trừ stock
            productService.deductStock(product, itemReq.getQuantity());
        }

        // 5. Gán danh sách items + tổng tiền
        order.setOrderItems(items);
        order.setTotalAmount(total);

        // 6. Lưu vào DB (cascade sẽ tự lưu OrderItem)
        return orderRepository.save(order);
    }

    public ResultPaginationDTO getOrdersByStatusPaging(
        String status,
        int page,
        int size) {

        // 1. Lấy user từ token
        String username = SecurityUtils.getCurrentUserLogin()
                .orElseThrow(() -> new RuntimeException("User not logged in"));

        User currentUser = userRepository.findByEmail(username);
        if (currentUser == null) {
            throw new RuntimeException("User not found");
        }

        RoleEnum role = RoleEnum.valueOf(currentUser.getRole().getName().toUpperCase());

        // 2. Validate status
        StatusEnum st;
        try {
            st = StatusEnum.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status: " + status);
        }

        // 3. Pageable sort theo createdAt DESC
        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Order> pageData;

        if (role == RoleEnum.USER) {
            // User chỉ xem đơn của TỰ MÌNH
            pageData = orderRepository.findByUserIdAndStatus(
                    currentUser.getId(), st, pageable);

        } else {
            // Admin xem tất cả đơn
            pageData = orderRepository.findByStatus(st, pageable);
        }

        // 4. Convert sang DTO
        Page<OrderDetailDTO> dtoPage = pageData.map(this::convertToOrderDetailDTO);

        // 5. Build Meta
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(dtoPage.getNumber());
        meta.setPageSize(dtoPage.getSize());
        meta.setPages(dtoPage.getTotalPages());
        meta.setTotal(dtoPage.getTotalElements());

        // 6. Build Response
        ResultPaginationDTO result = new ResultPaginationDTO();
        result.setMeta(meta);
        result.setResult(dtoPage.getContent());

        return result;
    }


    @Transactional
    public OrderDTO cancelOrder(Long orderId) {

        // 1. Lấy user hiện tại từ token
        String username = SecurityUtils.getCurrentUserLogin()
                .orElseThrow(() -> new RuntimeException("User not logged in"));
        User user = userRepository.findByEmail(username);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        RoleEnum role = RoleEnum.valueOf(user.getRole().getName().toUpperCase());

        // 2. Lấy order
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IdInvalidException("Order ID " + orderId + " không tồn tại"));

        // 3. Kiểm tra quyền trước khi xử lý
        if (role == RoleEnum.USER) {

            // USER chỉ thao tác trên đơn của mình
            if (!order.getUser().getId().equals(user.getId())) {
                throw new IdInvalidException("User không được phép thao tác đơn này");
            }

            // USER chỉ hủy đơn nếu đang PENDING
            if (order.getStatus() != StatusEnum.PENDING) {
                throw new IdInvalidException("User chỉ được hủy đơn khi trạng thái là PENDING");
            }
        }
        // Admin/Manager bỏ qua quyền

        // 4. Nếu đã hủy rồi → trả luôn (sau khi check quyền)
        if (order.getStatus() == StatusEnum.CANCELLED) {
            return convertToDTO(order);
        }

        // 5. Set trạng thái
        order.setStatus(StatusEnum.CANCELLED);
        orderRepository.save(order);

        // 6. Tăng lại stock
        order.getOrderItems().forEach(item -> {
            productService.increaseStock(item.getProduct(), item.getQuantity());
        });

        return convertToDTO(order);
    }

    public OrderDetailDTO getOrderDetail(Long orderId) {

        // 1. Lấy user hiện tại từ token
        String username = SecurityUtils.getCurrentUserLogin()
                .orElseThrow(() -> new RuntimeException("User not logged in"));

        User currentUser = userRepository.findByEmail(username);
        if (currentUser == null) {
            throw new RuntimeException("User not found");
        }

        RoleEnum role = RoleEnum.valueOf(currentUser.getRole().getName().toUpperCase());

        // 2. Lấy order
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IdInvalidException("Order ID " + orderId + " không tồn tại"));

        // 3. USER chỉ được xem order của chính họ
        if (role == RoleEnum.USER) {
            if (!order.getUser().getId().equals(currentUser.getId())) {
                throw new IdInvalidException("User không có quyền xem đơn này");
            }
        }

        // Admin xem được mọi đơn nên không cần check thêm

        // 4. Convert sang DTO
        return convertToOrderDetailDTO(order);
    }


    private OrderDetailDTO convertToOrderDetailDTO(Order order) {
        OrderDetailDTO dto = new OrderDetailDTO();

        dto.setOrderId(order.getId());
        dto.setShippingAddress(order.getShippingAddress());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus().name());

        // Set created / updated info
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());
        dto.setCreatedBy(order.getCreatedBy());
        dto.setUpdatedBy(order.getUpdatedBy());

        List<OrderItemDetailDTO> itemDtos = order.getOrderItems().stream()
                .map(this::convertItemToDetailDTO)
                .collect(Collectors.toList());

        dto.setItems(itemDtos);

        return dto;
    }

    private OrderItemDetailDTO convertItemToDetailDTO(OrderItem item) {
        Product p = item.getProduct();

        OrderItemDetailDTO dto = new OrderItemDetailDTO();
        dto.setProductId(p.getId());
        dto.setProductName(p.getName());
        dto.setProductType(item.getProductType());
        dto.setQuantity(item.getQuantity());
        dto.setPriceAtPurchase(item.getPriceAtPurchase());
        dto.setOldPrice(p.getOldPrice());

        // Lấy 1 URL ảnh đầu tiên từ DB
        String imageUrl = null;
        if (p.getImageUrl() != null && !p.getImageUrl().isEmpty()) {
            imageUrl = p.getImageUrl().get(0);
        }

        dto.setImageUrl(imageUrl);
        return dto;
    }

    // Public converter để controller có thể dùng
    public OrderDTO convertToDTO(Order order) {
        if (order == null)
            return null;
        OrderDTO dto = new OrderDTO();
        dto.setOrderId(order.getId());
        dto.setShippingAddress(order.getShippingAddress());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus() != null ? order.getStatus().name() : null);

        List<OrderItemDTO> items = new ArrayList<>();
        if (order.getOrderItems() != null) {
            for (OrderItem it : order.getOrderItems()) {
                OrderItemDTO idto = new OrderItemDTO();
                idto.setProductId(it.getProduct() != null ? it.getProduct().getId() : null);
                idto.setProductName(it.getProduct() != null ? it.getProduct().getName() : null);
                idto.setProductType(it.getProductType());
                idto.setQuantity(it.getQuantity());
                idto.setPriceAtPurchase(it.getPriceAtPurchase());
                items.add(idto);
            }
        }
        dto.setItems(items);
        return dto;
    }


    public ResultPaginationDTO getMyOrdersByStatus(
            String status,
            int page,
            int size) {

        // 1. Lấy user từ token
        String username = SecurityUtils.getCurrentUserLogin()
                .orElseThrow(() -> new RuntimeException("User not logged in"));

        User currentUser = userRepository.findByEmail(username);
        if (currentUser == null) {
            throw new RuntimeException("User not found");
        }

        // 2. Validate status
        StatusEnum statusEnum;
        try {
            statusEnum = StatusEnum.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status: " + status);
        }

        // 3. Pageable sort theo createdAt DESC
        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        // 4. Lấy đơn của USER này theo status
        Page<Order> pageData = orderRepository.findByUserIdAndStatus(
                currentUser.getId(), statusEnum, pageable);

        // 5. Convert sang DTO
        Page<OrderDetailDTO> dtoPage = pageData.map(this::convertToOrderDetailDTO);

        // 6. Build Response
        return buildPaginationResult(dtoPage);
    }

    public ResultPaginationDTO getAllOrdersByStatusForAdmin(
            String status,
            int page,
            int size) {

        // 1. Lấy user từ token và check role
        String username = SecurityUtils.getCurrentUserLogin()
                .orElseThrow(() -> new RuntimeException("User not logged in"));

        User currentUser = userRepository.findByEmail(username);
        if (currentUser == null) {
            throw new RuntimeException("User not found");
        }

        // 2. Check role ADMIN
        RoleEnum role = RoleEnum.valueOf(currentUser.getRole().getName().toUpperCase());
        if (role != RoleEnum.ADMIN) {
            throw new IdInvalidException("Only ADMIN can access all orders");
        }

        // 3. Pageable sort theo createdAt DESC
        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Order> pageData;

        // 4. Nếu status == null hoặc empty → lấy TẤT CẢ
        if (status == null || status.trim().isEmpty()) {
            pageData = orderRepository.findAll(pageable);
        } else {
            // 5. Nếu có status → validate và filter theo status
            StatusEnum statusEnum;
            try {
                statusEnum = StatusEnum.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid status: " + status);
            }
            pageData = orderRepository.findByStatus(statusEnum, pageable);
        }

        // 6. Convert sang DTO
        Page<OrderDetailDTO> dtoPage = pageData.map(this::convertToOrderDetailDTO);

        // 7. Build Response
        return buildPaginationResult(dtoPage);
    }

    private ResultPaginationDTO buildPaginationResult(Page<OrderDetailDTO> dtoPage) {
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(dtoPage.getNumber());
        meta.setPageSize(dtoPage.getSize());
        meta.setPages(dtoPage.getTotalPages());
        meta.setTotal(dtoPage.getTotalElements());

        ResultPaginationDTO result = new ResultPaginationDTO();
        result.setMeta(meta);
        result.setResult(dtoPage.getContent());

        return result;
    }


    @Transactional
    public OrderDTO cancelMyOrder(Long orderId) {
        // 1. Lấy user hiện tại từ token
        String username = SecurityUtils.getCurrentUserLogin()
                .orElseThrow(() -> new RuntimeException("User not logged in"));
        User user = userRepository.findByEmail(username);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        // 2. Lấy order
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IdInvalidException("Order ID " + orderId + " không tồn tại"));

        // 3. Kiểm tra quyền: USER chỉ thao tác trên đơn của mình
        if (!order.getUser().getId().equals(user.getId())) {
            throw new IdInvalidException("Bạn không có quyền hủy đơn hàng này");
        }

        // 4. Kiểm tra trạng thái: USER chỉ hủy được khi PENDING
        if (order.getStatus() != StatusEnum.PENDING) {
            throw new IdInvalidException("Chỉ có thể hủy đơn hàng khi trạng thái là PENDING");
        }

        // 5. Nếu đã CANCELLED rồi → trả luôn
        if (order.getStatus() == StatusEnum.CANCELLED) {
            return convertToDTO(order);
        }

        // 6. Set trạng thái CANCELLED
        order.setStatus(StatusEnum.CANCELLED);
        orderRepository.save(order);

        // 7. Hoàn lại stock
        order.getOrderItems().forEach(item -> {
            productService.increaseStock(item.getProduct(), item.getQuantity());
        });

        return convertToDTO(order);
    }

    @Transactional
    public OrderDTO updateOrderStatus(Long orderId, String newStatus) {

        // 1. Lấy user và check role ADMIN
        String username = SecurityUtils.getCurrentUserLogin()
                .orElseThrow(() -> new RuntimeException("User not logged in"));
        User user = userRepository.findByEmail(username);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        RoleEnum role = RoleEnum.valueOf(user.getRole().getName().toUpperCase());
        if (role != RoleEnum.ADMIN) {
            throw new IdInvalidException("Chỉ ADMIN mới có quyền cập nhật trạng thái đơn hàng");
        }

        // 2. Validate status
        StatusEnum statusEnum;
        try {
            statusEnum = StatusEnum.valueOf(newStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IdInvalidException("Trạng thái không hợp lệ: " + newStatus);
        }

        // 3. Lấy order
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IdInvalidException("Order ID " + orderId + " không tồn tại"));

        StatusEnum oldStatus = order.getStatus();

        // 4. Validate business logic theo flow
        validateStatusTransition(oldStatus, statusEnum);

        // 5. Nếu chuyển sang CANCELLED → hoàn lại stock
        if (statusEnum == StatusEnum.CANCELLED && oldStatus != StatusEnum.CANCELLED) {
            order.getOrderItems().forEach(item -> {
                productService.increaseStock(item.getProduct(), item.getQuantity());
            });
        }

        // 6. Cập nhật status
        order.setStatus(statusEnum);
        orderRepository.save(order);

        return convertToDTO(order);
    }

    /**
     * Validate business logic khi chuyển status
     */
    private void validateStatusTransition(StatusEnum oldStatus, StatusEnum newStatus) {
        // Nếu đã CANCELLED hoặc DELIVERED → không cho phép thay đổi
        if (oldStatus == StatusEnum.CANCELLED) {
            throw new IdInvalidException("Không thể thay đổi trạng thái của đơn hàng đã hủy");
        }
        if (oldStatus == StatusEnum.DELIVERED) {
            throw new IdInvalidException("Không thể thay đổi trạng thái của đơn hàng đã giao");
        }

        // Flow chuẩn: PENDING → CONFIRMED → SHIPPING → SHIPPED → DELIVERED
        // Hoặc có thể CANCEL ở bất kỳ bước nào
        // Không cho phép quay lại trạng thái trước đó (trừ CANCELLED)
        if (newStatus != StatusEnum.CANCELLED) {
            int oldOrder = getStatusOrder(oldStatus);
            int newOrder = getStatusOrder(newStatus);
            if (newOrder <= oldOrder) {
                throw new IdInvalidException(
                    "Không thể chuyển từ " + oldStatus + " sang " + newStatus);
            }
        }
    }

    /**
     * Helper để xác định thứ tự status
     */
    private int getStatusOrder(StatusEnum status) {
        switch (status) {
            case PENDING: return 1;
            case CONFIRMED: return 2;
            case SHIPPING: return 3;
            case SHIPPED: return 4;
            case DELIVERED: return 5;
            case CANCELLED: return 0; // Special case
            default: return -1;
        }
    }
}
