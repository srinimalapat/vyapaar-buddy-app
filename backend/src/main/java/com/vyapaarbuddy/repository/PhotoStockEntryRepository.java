package com.vyapaarbuddy.repository;

import com.vyapaarbuddy.entity.PhotoStockEntry;
import com.vyapaarbuddy.enums.PhotoStockEntryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PhotoStockEntryRepository extends JpaRepository<PhotoStockEntry, Long> {

    Optional<PhotoStockEntry> findByBusinessIdAndId(Long businessId, Long id);

    List<PhotoStockEntry> findByBusinessIdOrderByCreatedAtDesc(Long businessId);

    List<PhotoStockEntry> findByBusinessIdAndStatusOrderByCreatedAtDesc(Long businessId, PhotoStockEntryStatus status);
}
