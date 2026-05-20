package com.vyapaarbuddy.repository;

import com.vyapaarbuddy.entity.InventoryItem;
import com.vyapaarbuddy.enums.InventoryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

    List<InventoryItem> findByBusinessId(Long businessId);

    Optional<InventoryItem> findByBusinessIdAndId(Long businessId, Long id);

    List<InventoryItem> findByBusinessIdAndStatus(Long businessId, InventoryStatus status);

    List<InventoryItem> findByBusinessIdAndStatusAndNameContainingIgnoreCase(
            Long businessId, InventoryStatus status, String name);

    List<InventoryItem> findByBusinessIdAndNameContainingIgnoreCase(Long businessId, String name);

    Optional<InventoryItem> findByBusinessIdAndNameIgnoreCaseAndStatus(
            Long businessId, String name, InventoryStatus status);

    boolean existsByBusinessIdAndNameIgnoreCaseAndStatus(
            Long businessId, String name, InventoryStatus status);

    @Query("SELECT i FROM InventoryItem i WHERE i.business.id = :businessId AND i.status = 'ACTIVE' AND i.quantity <= i.lowStockThreshold")
    List<InventoryItem> findActiveLowStockItems(@Param("businessId") Long businessId);

    @Query("SELECT i FROM InventoryItem i WHERE i.business.id = :businessId AND i.quantity <= i.lowStockThreshold")
    List<InventoryItem> findLowStockItems(@Param("businessId") Long businessId);

    @Query("SELECT i FROM InventoryItem i WHERE i.business.id = :businessId AND i.quantity = 0")
    List<InventoryItem> findOutOfStockItems(@Param("businessId") Long businessId);
}
