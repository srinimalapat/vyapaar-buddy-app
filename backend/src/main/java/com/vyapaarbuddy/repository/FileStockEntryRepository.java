package com.vyapaarbuddy.repository;

import com.vyapaarbuddy.entity.FileStockEntry;
import com.vyapaarbuddy.enums.FileStockEntryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileStockEntryRepository extends JpaRepository<FileStockEntry, Long> {

    Optional<FileStockEntry> findByBusinessIdAndId(Long businessId, Long id);

    List<FileStockEntry> findByBusinessIdOrderByCreatedAtDesc(Long businessId);

    List<FileStockEntry> findByBusinessIdAndStatusOrderByCreatedAtDesc(Long businessId, FileStockEntryStatus status);
}
