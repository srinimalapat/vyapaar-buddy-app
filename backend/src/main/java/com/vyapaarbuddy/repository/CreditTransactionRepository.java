package com.vyapaarbuddy.repository;

import com.vyapaarbuddy.entity.CreditTransaction;
import com.vyapaarbuddy.enums.CreditTransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface CreditTransactionRepository extends JpaRepository<CreditTransaction, Long> {

    List<CreditTransaction> findByBusinessIdAndCustomerIdOrderByTransactionDateDescCreatedAtDesc(
            Long businessId, Long customerId);

    List<CreditTransaction> findByBusinessIdOrderByTransactionDateDescCreatedAtDesc(Long businessId);

    Page<CreditTransaction> findByBusinessId(Long businessId, Pageable pageable);

    Page<CreditTransaction> findByBusinessIdAndCustomerId(Long businessId, Long customerId, Pageable pageable);

    List<CreditTransaction> findByBusinessIdAndCustomerIdAndIsSettled(Long businessId, Long customerId, boolean isSettled);

    @Query("SELECT ct FROM CreditTransaction ct WHERE ct.business.id = :businessId AND ct.isSettled = false AND ct.dueDate < :date")
    List<CreditTransaction> findOverdueCredits(@Param("businessId") Long businessId, @Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(ct.amount), 0) FROM CreditTransaction ct WHERE ct.business.id = :businessId AND ct.customer.id = :customerId AND ct.isSettled = false")
    BigDecimal findOutstandingBalance(@Param("businessId") Long businessId, @Param("customerId") Long customerId);

    @Query("SELECT COALESCE(SUM(ct.amount), 0) FROM CreditTransaction ct WHERE ct.business.id = :businessId AND ct.isSettled = false")
    BigDecimal findTotalOutstandingCredit(@Param("businessId") Long businessId);

    List<CreditTransaction> findTop5ByBusinessIdAndTypeOrderByCreatedAtDesc(
            Long businessId, CreditTransactionType type);
}
