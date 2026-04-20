package com.example.demo.repository;

import com.example.demo.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    // Spring Data JPA sẽ tự động hiểu hàm này để tìm Role theo tên
    Optional<Role> findByName(String name);
}