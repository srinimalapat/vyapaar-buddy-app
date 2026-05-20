package com.vyapaarbuddy.repository;

import com.vyapaarbuddy.entity.Customer;
import com.vyapaarbuddy.enums.CustomerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    List<Customer> findByBusinessId(Long businessId);

    Page<Customer> findByBusinessId(Long businessId, Pageable pageable);

    Optional<Customer> findByBusinessIdAndId(Long businessId, Long id);

    Optional<Customer> findByBusinessIdAndPhone(Long businessId, String phone);

    List<Customer> findByBusinessIdAndStatus(Long businessId, CustomerStatus status);

    List<Customer> findByBusinessIdAndStatusAndCreditBalanceGreaterThan(
            Long businessId, CustomerStatus status, BigDecimal amount);

    List<Customer> findByBusinessIdAndCreditBalanceGreaterThan(Long businessId, BigDecimal amount);

    @Query("SELECT c FROM Customer c WHERE c.business.id = :businessId AND c.creditBalance > 0")
    List<Customer> findCustomersWithOutstandingCredit(@Param("businessId") Long businessId);

    @Query("SELECT COALESCE(SUM(c.creditBalance), 0) FROM Customer c WHERE c.business.id = :businessId AND c.creditBalance > 0")
    BigDecimal sumOutstandingCreditByBusinessId(@Param("businessId") Long businessId);

    @Query("SELECT COUNT(c) FROM Customer c WHERE c.business.id = :businessId AND c.creditBalance > 0")
    Long countCustomersWithOutstandingCredit(@Param("businessId") Long businessId);

    @Query("SELECT c FROM Customer c WHERE c.business.id = :businessId AND (LOWER(c.name) LIKE LOWER(CONCAT('%',:keyword,'%')) OR c.phone LIKE CONCAT('%',:keyword,'%'))")
    List<Customer> searchCustomersByBusinessId(@Param("businessId") Long businessId, @Param("keyword") String keyword);

    @Query("SELECT c FROM Customer c WHERE c.business.id = :businessId AND (LOWER(c.name) LIKE LOWER(CONCAT('%',:keyword,'%')) OR c.phone LIKE CONCAT('%',:keyword,'%'))")
    Page<Customer> searchCustomers(@Param("businessId") Long businessId, @Param("keyword") String keyword, Pageable pageable);
}
