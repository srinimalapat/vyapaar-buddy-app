package com.vyapaarbuddy.repository;

import com.vyapaarbuddy.entity.WhatsAppMessageLog;
import com.vyapaarbuddy.enums.WhatsAppMessageDirection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WhatsAppMessageLogRepository extends JpaRepository<WhatsAppMessageLog, Long> {

    List<WhatsAppMessageLog> findByMobileNumberOrderByCreatedAtDesc(String mobileNumber);

    List<WhatsAppMessageLog> findByDirectionOrderByCreatedAtDesc(WhatsAppMessageDirection direction);
}
