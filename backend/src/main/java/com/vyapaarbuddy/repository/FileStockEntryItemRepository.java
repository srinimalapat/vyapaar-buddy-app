package com.vyapaarbuddy.repository;

import com.vyapaarbuddy.entity.FileStockEntryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileStockEntryItemRepository extends JpaRepository<FileStockEntryItem, Long> {

    List<FileStockEntryItem> findByFileStockEntryId(Long fileStockEntryId);
}
