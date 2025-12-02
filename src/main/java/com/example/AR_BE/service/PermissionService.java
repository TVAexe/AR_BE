package com.example.AR_BE.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.example.AR_BE.domain.Permission;
import com.example.AR_BE.domain.response.ResultPaginationDTO;
import com.example.AR_BE.repository.PermissionRepository;

@Service
public class PermissionService {
    private final PermissionRepository permissionRepository;

    public PermissionService(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    public boolean isPermissionExist(Permission permission) {
        return this.permissionRepository.existsByModuleAndApiPathAndMethod(
            permission.getModule(),
            permission.getApiPath(),
            permission.getMethod()
        );
    }

    public Permission createPermission(Permission permission) {
        return this.permissionRepository.save(permission);
    }

    public Permission fetchById(Long id) {
        Optional<Permission> permission = this.permissionRepository.findById(id);
        if (permission.isPresent()) {
            return permission.get();
        }
        return null;
    }

    public Permission updatePermission(Permission permission) {
        Permission existingPermission = this.fetchById(permission.getId());
        if (existingPermission != null) {
            existingPermission.setName(permission.getName());
            existingPermission.setApiPath(permission.getApiPath());
            existingPermission.setMethod(permission.getMethod());
            existingPermission.setModule(permission.getModule());
            return this.permissionRepository.save(existingPermission);
        }
        return null;
    }

    public void deletePermission(Long id) {
        Optional<Permission> permission = this.permissionRepository.findById(id);
        Permission existingPermission = permission.get();
        existingPermission.getRoles().forEach(role -> {
            role.getPermissions().remove(existingPermission);
        });
        this.permissionRepository.delete(existingPermission);
    }

    public ResultPaginationDTO getAllPermissions(Specification<Permission> spec, Pageable pageable) {
        // TODO Auto-generated method stub
        Page<Permission> pagePermissions = this.permissionRepository.findAll(spec, pageable);
        ResultPaginationDTO result = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(pagePermissions.getTotalPages());
        meta.setTotal(pagePermissions.getTotalElements());
        result.setMeta(meta);
        result.setResult(pagePermissions.getContent());
        return result;
    }
}
