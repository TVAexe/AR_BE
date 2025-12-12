package com.example.AR_BE.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.AR_BE.domain.Role;
import com.example.AR_BE.domain.User;
import com.example.AR_BE.repository.RoleRepository;
import com.example.AR_BE.repository.UserRepository;
import com.example.AR_BE.utils.constants.GenderEnum;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DatabaseInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // ===== SEED ROLES =====
        if (roleRepository.count() == 0) {
            System.out.println("Seeding roles...");
            
            // Role USER
            Role userRole = new Role();
            userRole.setName("USER");
            userRole.setDescription("Người dùng thông thường");
            userRole.setActive(true);
            roleRepository.save(userRole);
           // Role ADMIN
            Role adminRole = new Role();
            adminRole.setName("ADMIN");
            adminRole.setDescription("Quản trị viên hệ thống");
            adminRole.setActive(true);
            roleRepository.save(adminRole);
            
            System.out.println("✅ Roles seeded successfully!");
        }
        
        // ===== SEED USERS =====
        if (userRepository.count() == 0) {
            System.out.println("Seeding users...");
            
            Role userRole = roleRepository.findByName("USER").orElseThrow();
            Role adminRole = roleRepository.findByName("ADMIN").orElseThrow();
            
            // Admin User
            User admin = new User();
            admin.setName("Admin User");
            admin.setEmail("admin@gmail.com");
            admin.setPassword(passwordEncoder.encode("123456"));
            admin.setPhoneNumber("0900000001");
            admin.setAge(30);
            admin.setGender(GenderEnum.MALE);
            admin.setAddress("123 Admin Street, Ho Chi Minh City");
            admin.setRole(adminRole);
            userRepository.save(admin);
            
            // Regular User
            User regularUser = new User();
            regularUser.setName("Regular User");
            regularUser.setEmail("user@gmail.com");
            regularUser.setPassword(passwordEncoder.encode("123456"));
            regularUser.setPhoneNumber("0900000002");
            regularUser.setAge(25);
            regularUser.setGender(GenderEnum.FEMALE);
            regularUser.setAddress("456 User Avenue, Ho Chi Minh City");
            regularUser.setRole(userRole);
            userRepository.save(regularUser);
            
            System.out.println("✅ Users seeded successfully!");
            System.out.println("Admin account: admin@example.com / 123456");
            System.out.println("User account: user@example.com / 123456");
        }
    }
}