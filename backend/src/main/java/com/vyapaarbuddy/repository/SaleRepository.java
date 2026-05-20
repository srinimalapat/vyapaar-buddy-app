package com.vyapaarbuddy.repository;

import com.vyapaarbuddy.entity.Sale;
import com.vyapaarbuddy.enums.SaleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {

    Optional<Sale> findByBusinessIdAndId(Long businessId, Long id);

    List<Sale> findByBusinessIdOrderBySaleDateDescCreatedAtDesc(Long businessId);

    List<Sale> findByBusinessIdAndSaleDateBetweenOrderBySaleDateDescCreatedAtDesc(
            Long businessId, LocalDate from, LocalDate to);

    List<Sale> findByBusinessIdAndCustomerIdOrderBySaleDateDescCreatedAtDesc(Long businessId, Long customerId);

    List<Sale> findByBusinessIdAndTypeOrderBySaleDateDescCreatedAtDesc(Long businessId, SaleType type);

    List<Sale> findByBusinessIdAndSaleDate(Long businessId, LocalDate saleDate);

    List<Sale> findByBusinessIdAndSaleDateBetween(Long businessId, LocalDate startDate, LocalDate endDate);

    Page<Sale> findByBusinessId(Long businessId, Pageable pageable);

    Page<Sale> findByBusinessIdAndCustomerId(Long businessId, Long customerId, Pageable pageable);

    Page<Sale> findByBusinessIdAndSaleDateBetween(Long businessId, LocalDate startDate, LocalDate endDate, Pageable pageable);

    @Query("SELECT SUM(s.totalAmount) FROM Sale s WHERE s.business.id = :businessId AND s.saleDate = :date")
    BigDecimal findTotalSalesByDate(@Param("businessId") Long businessId, @Param("date") LocalDate date);

    @Query("SELECT COUNT(s) FROM Sale s WHERE s.business.id = :businessId AND s.saleDate = :date")
    Long findSalesCountByDate(@Param("businessId") Long businessId, @Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(s.totalAmount), 0) FROM Sale s WHERE s.business.id = :businessId AND s.type = :type AND s.saleDate BETWEEN :from AND :to")
    BigDecimal sumByBusinessIdAndTypeAndDateRange(
            @Param("businessId") Long businessId,
            @Param("type") SaleType type,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    List<Sale> findTop5ByBusinessIdOrderByCreatedAtDesc(Long businessId);

    @Query("SELECT COALESCE(SUM(s.totalAmount), 0) FROM Sale s WHERE s.business.id = :businessId AND s.saleDate BETWEEN :from AND :to")
    BigDecimal sumTotalByBusinessIdAndDateRange(
            @Param("businessId") Long businessId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);
}
