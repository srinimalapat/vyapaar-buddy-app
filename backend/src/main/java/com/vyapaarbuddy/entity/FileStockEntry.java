package com.vyapaarbuddy.entity;

import com.vyapaarbuddy.enums.FileStockEntryStatus;
import com.vyapaarbuddy.enums.FileStockSourceType;
import com.vyapaarbuddy.enums.FileStockType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "file_stock_entries",
        indexes = {
            @Index(name = "idx_fse_business_id", columnList = "business_id"),
            @Index(name = "idx_fse_status",      columnList = "status"),
            @Index(name = "idx_fse_file_type",   columnList = "file_type"),
            @Index(name = "idx_fse_created_at",  columnList = "created_at")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = "items")
@ToString(exclude = "items")
public class FileStockEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_user_id", nullable = false)
    private User uploadedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FileStockSourceType sourceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", nullable = false)
    private FileStockType fileType;

    private String originalFileName;
    private String storedFilePath;
    private String contentType;
    private Long fileSize;

    @Column(columnDefinition = "TEXT")
    private String extractedText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FileStockEntryStatus status;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Builder.Default
    @OneToMany(mappedBy = "fileStockEntry", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FileStockEntryItem> items = new ArrayList<>();

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
