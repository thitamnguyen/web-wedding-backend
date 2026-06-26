//package com.example.demo.config;
//
//import com.example.demo.model.Role;
//import com.example.demo.repository.RoleRepository;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.core.annotation.Order;
//import org.springframework.core.annotation.Order;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//
//@Component
//@Order(0)
//public class RoleDataInitializer implements CommandLineRunner {
//
//    private final RoleRepository roleRepository;
//
//    public RoleDataInitializer(RoleRepository roleRepository) {
//        this.roleRepository = roleRepository;
//    }
//
//    @Override
//    public void run(String... args) {
//        ensureRole("ROLE_CLIENT", "Khách hàng");
//        ensureRole("ROLE_ADMIN", "Quản trị viên");
//        ensureRole("ROLE_STAFF", "Nhân viên");
//    }
//
//    private void ensureRole(String name, String description) {
//        roleRepository.findByName(name).orElseGet(() -> {
//            Role role = new Role();
//            role.setName(name);
//            role.setDescription(description);
//            return roleRepository.save(role);
//        });
//    }
//}
