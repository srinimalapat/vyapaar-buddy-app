package com.vyapaarbuddy.repository;

import com.vyapaarbuddy.entity.User;
import com.vyapaarbuddy.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for User entity.
 * TODO: Add custom query methods for user search
 * TODO: Add query for finding users by role
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
