package com.example.AR_BE.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import com.turkraft.springfilter.boot.Filter;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.AR_BE.domain.Permission;
import com.example.AR_BE.domain.response.ResultPaginationDTO;
import com.example.AR_BE.service.PermissionService;
import com.example.AR_BE.utils.annotation.ApiMessage;
import com.example.AR_BE.utils.exception.IdInvalidException;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/v1")
public class PermissionController {
    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @PostMapping("/permissions")
    @ApiMessage("Create Permission")
    public ResponseEntity<Permission> createPermission(@Valid @RequestBody Permission permission) throws IdInvalidException {
       if (this.permissionService.isPermissionExist(permission)) {
            throw new IdInvalidException("Permission already exists");
       }
       Permission createdPermission = this.permissionService.createPermission(permission);
       return ResponseEntity.status(HttpStatus.CREATED).body(createdPermission);
    }

    @PutMapping("/permissions")
    @ApiMessage("Update Permission")
    public ResponseEntity<Permission> updatePermission(@RequestBody Permission permission) throws IdInvalidException {
        if (this.permissionService.fetchById(permission.getId()) == null) {
            throw new IdInvalidException("Permission with id " + permission.getId() + " does not exist");
        }

        Permission updatedPermission = this.permissionService.updatePermission(permission);
        return ResponseEntity.ok(updatedPermission);
    }

    @DeleteMapping("/permissions/{id}")
    @ApiMessage("Delete Permission")
    public ResponseEntity<Void> deletePermission(@PathVariable("id") Long id) throws IdInvalidException {
        Permission existingPermission = this.permissionService.fetchById(id);
        if (existingPermission == null) {
            throw new IdInvalidException("Permission with id " + id + " does not exist");
        }
        this.permissionService.deletePermission(id);
        return ResponseEntity.ok().body(null);
    }

    @GetMapping("/permissions")
    public ResponseEntity<ResultPaginationDTO> getAllPermissions(@Filter Specification<Permission> spec, Pageable pageable) {
        return ResponseEntity.ok(this.permissionService.getAllPermissions(spec, pageable));
    }
}
