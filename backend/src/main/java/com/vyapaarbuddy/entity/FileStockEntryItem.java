package com.vyapaarbuddy.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "file_stock_entry_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = "fileStockEntry")
@ToString(exclude = "fileStockEntry")
public class FileStockEntryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_stock_entry_id", nullable = false)
    private FileStockEntry fileStockEntry;

    @Column(nullable = false)
    private String itemName;

    @Column(precision = 15, scale = 3)
    private BigDecimal quantity;

    private String unit;

    @Column(precision = 15, scale = 2)
    private BigDecimal unitPrice;

    @Builder.Default
    private String category = "General";

    @Column(precision = 5, scale = 2)
    private BigDecimal confidenceScore;

    @Column(columnDefinition = "TEXT")
    private String validationErrors;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
