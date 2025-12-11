package com.example.AR_BE.service;

import com.example.AR_BE.domain.request.UpdatePasswordDTORequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.AR_BE.domain.Role;
import com.example.AR_BE.domain.User;
import com.example.AR_BE.domain.response.FetchUserDTOResponse;
import com.example.AR_BE.domain.response.NewUserDTOResponse;
import com.example.AR_BE.domain.response.ResultPaginationDTO;
import com.example.AR_BE.domain.response.UpdateUserDTOResponse;
import com.example.AR_BE.domain.response.UserListDTOResponse;
import com.example.AR_BE.repository.RoleRepository;
import com.example.AR_BE.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    public UserService(UserRepository userRepository, RoleService roleService, 
                      PasswordEncoder passwordEncoder, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;

    }

    public User handleCreateUser(User user) {
        if (user.getRole() != null) {
            Role role = this.roleService.fetchById(user.getRole().getId());
            if (role != null) {
                user.setRole(role);
            } else {
                user.setRole(null);
            }
        }
        return userRepository.save(user);
    }

    // ✅ Method mới: Tạo user với role USER mặc định (dùng cho register)
    public User handleCreateUserWithDefaultRole(User user) {
        // Tìm role USER từ database
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Role USER not found. Please run database seeder."));
        
        // Gán role USER cho user
        user.setRole(userRole);
        
        return userRepository.save(user);
    }

    public boolean checkEmailExist(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean checkPhoneExist(String phoneNumber) {
        return userRepository.existsByPhoneNumber(phoneNumber);
    }

    public User findUserByUsername(String username) {
        return this.userRepository.findByEmail(username);
    }

    public void updateUserToken(String token, String email) {
        User user = this.findUserByUsername(email);
        if (user != null) {
            user.setRefreshToken(token);
            this.userRepository.save(user);
        }
    }

    public NewUserDTOResponse convertToNewUserDTOResponse(User user) {
        NewUserDTOResponse newUserDTOResponse = new NewUserDTOResponse();
        NewUserDTOResponse.RoleUser roleUser = new NewUserDTOResponse.RoleUser();
        newUserDTOResponse.setId(user.getId());
        newUserDTOResponse.setEmail(user.getEmail());
        newUserDTOResponse.setName(user.getName());
        newUserDTOResponse.setPhoneNumber(user.getPhoneNumber());
        newUserDTOResponse.setCreatedAt(user.getCreatedAt());
        if (user.getRole() != null) {
            roleUser.setId(user.getRole().getId());
            roleUser.setName(user.getRole().getName());
            newUserDTOResponse.setRole(roleUser);
        }
        return newUserDTOResponse;
    }

    public void handleDeleteUser(long id) {
        this.userRepository.deleteById(id);
    }

    public User handleGetUser(long id) {
        Optional<User> userOptional = this.userRepository.findById(id);
        return userOptional.orElse(null);
    }

    public void changePasswordByEmail(String email, UpdatePasswordDTORequest req) {
        User user = userRepository.findByEmail(email);
        if (user == null) return;

        if (req.getOldPassword() != null &&
                !passwordEncoder.matches(req.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Wrong old password");
        }

        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
    }

    public User handleUpdateUser(User user) {
        User currentUser = this.handleGetUser(user.getId());
        if (currentUser != null) {
            currentUser.setName(user.getName());
            currentUser.setEmail(user.getEmail());
            currentUser.setAddress(user.getAddress());
            currentUser.setAge(user.getAge());
            currentUser.setGender(user.getGender());
            currentUser.setPhoneNumber(user.getPhoneNumber());

            if (user.getRole() != null) {
                Role role = this.roleService.fetchById(user.getRole().getId());
                if (role != null) {
                    currentUser.setRole(role);
                } else {
                    currentUser.setRole(null);
                }
            } else {
                currentUser.setRole(null);
            }
            currentUser = this.userRepository.save(currentUser);
        }
        return currentUser;
    }

    public ResultPaginationDTO handleGetAllUsers(Specification<User> specification, Pageable pageable) {
        Page<User> pageUser = this.userRepository.findAll(specification, pageable);
        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(pageUser.getTotalPages());
        meta.setTotal(pageUser.getTotalElements());

        rs.setMeta(meta);

        // Convert to UserListDTOResponse
        List<UserListDTOResponse> userList = pageUser.getContent().stream()
            .map(this::convertToUserListDTO)
            .collect(Collectors.toList());
        rs.setResult(userList);
        return rs;
    }

    public UserListDTOResponse convertToUserListDTO(User user) {
        UserListDTOResponse dto = new UserListDTOResponse();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setAge(user.getAge());
        dto.setGender(user.getGender() != null ? user.getGender().name() : null);
        dto.setCreatedAt(user.getCreatedAt());
        dto.setCreatedBy(user.getCreatedBy());
        
        if (user.getRole() != null) {
            UserListDTOResponse.RoleResponse roleResponse = new UserListDTOResponse.RoleResponse();
            roleResponse.setId(user.getRole().getId());
            roleResponse.setName(user.getRole().getName());
            dto.setRole(roleResponse);
        }
        
        return dto;
    }

    public FetchUserDTOResponse convertFetchUserDTO(User user) {
        FetchUserDTOResponse fetchUserDTO = new FetchUserDTOResponse();
        fetchUserDTO.setId(user.getId());
        fetchUserDTO.setEmail(user.getEmail());
        fetchUserDTO.setName(user.getName());
        fetchUserDTO.setUpdatedAt(user.getUpdatedAt());
        fetchUserDTO.setCreatedAt(user.getCreatedAt());
        return fetchUserDTO;
    }

    public UpdateUserDTOResponse convertUpdateUserDTO(User user) {
        UpdateUserDTOResponse updateUserDTO = new UpdateUserDTOResponse();
        updateUserDTO.setEmail(user.getEmail());
        updateUserDTO.setId(user.getId());
        updateUserDTO.setName(user.getName());
        updateUserDTO.setAddress(user.getAddress());
        updateUserDTO.setAge(user.getAge());
        updateUserDTO.setUpdatedAt(user.getUpdatedAt());
        return updateUserDTO;
    }
}