package com.vyapaarbuddy.repository;

import com.vyapaarbuddy.entity.PhotoStockEntryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhotoStockEntryItemRepository extends JpaRepository<PhotoStockEntryItem, Long> {

    List<PhotoStockEntryItem> findByPhotoStockEntryId(Long photoStockEntryId);
}
