package com.vyapaarbuddy.entity;

import com.vyapaarbuddy.enums.ReminderChannel;
import com.vyapaarbuddy.enums.ReminderStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reminders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    // nullable — reminders can be generated without a specific credit transaction
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credit_transaction_id")
    private CreditTransaction creditTransaction;

    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal amount;

    private LocalDate dueDate;

    private LocalDateTime scheduledDate;

    private LocalDateTime sentDate;

    @Enumerated(EnumType.STRING)
    private ReminderChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReminderStatus status;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Builder.Default
    private Integer retryCount = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
