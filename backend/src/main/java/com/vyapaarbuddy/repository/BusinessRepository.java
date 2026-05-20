package com.vyapaarbuddy.repository;

import com.vyapaarbuddy.entity.Business;
import com.vyapaarbuddy.enums.BusinessType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BusinessRepository extends JpaRepository<Business, Long> {

    Optional<Business> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    Optional<Business> findByUserIdAndId(Long userId, Long id);

    List<Business> findByType(BusinessType type);
}
