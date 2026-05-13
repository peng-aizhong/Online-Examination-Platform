package com.examination.repository;

import com.examination.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    List<User> findByRoleAndActiveTrue(User.UserRole role);
    Page<User> findByRole(User.UserRole role, Pageable pageable);
    Page<User> findByActive(Boolean active, Pageable pageable);
    Page<User> findByRoleAndActive(User.UserRole role, Boolean active, Pageable pageable);
}
