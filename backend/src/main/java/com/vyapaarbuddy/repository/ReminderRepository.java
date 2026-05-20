package com.vyapaarbuddy.repository;

import com.vyapaarbuddy.entity.Reminder;
import com.vyapaarbuddy.enums.ReminderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Long> {

    List<Reminder> findByBusinessId(Long businessId);

    Optional<Reminder> findByBusinessIdAndId(Long businessId, Long id);

    List<Reminder> findByBusinessIdAndStatus(Long businessId, ReminderStatus status);

    List<Reminder> findByBusinessIdAndCustomerId(Long businessId, Long customerId);

    List<Reminder> findByBusinessIdAndStatusAndCustomerId(
            Long businessId, ReminderStatus status, Long customerId);

    @Query("SELECT r FROM Reminder r WHERE r.business.id = :businessId AND r.status = 'PENDING' AND r.scheduledDate <= :dateTime")
    List<Reminder> findRemindersToSend(@Param("businessId") Long businessId,
                                       @Param("dateTime") LocalDateTime dateTime);
}
