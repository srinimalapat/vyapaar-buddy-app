package com.vyapaarbuddy.entity;

import com.vyapaarbuddy.enums.BusinessType;
import com.vyapaarbuddy.enums.PreferredLanguage;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "businesses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Business {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String ownerName;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    private BusinessType type;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String address;

    private String city;

    private String state;

    private String pinCode;

    private String gstNumber;

    @Column(nullable = false)
    private String phone;

    @Enumerated(EnumType.STRING)
    private PreferredLanguage preferredLanguage;

    private String timezone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

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
