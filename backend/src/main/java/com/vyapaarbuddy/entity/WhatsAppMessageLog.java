package com.vyapaarbuddy.entity;

import com.vyapaarbuddy.enums.WhatsAppMessageDirection;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "whatsapp_message_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WhatsAppMessageLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id")
    private Business business;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    private String provider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WhatsAppMessageDirection direction;

    private String waMessageId;

    private String mobileNumber;

    @Column(columnDefinition = "TEXT")
    private String messageBody;

    private String messageType;

    private String status;

    @Column(columnDefinition = "TEXT")
    private String rawPayload;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
