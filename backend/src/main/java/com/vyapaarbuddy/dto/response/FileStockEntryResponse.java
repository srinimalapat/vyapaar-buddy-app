package com.vyapaarbuddy.dto.response;

import com.vyapaarbuddy.enums.FileStockEntryStatus;
import com.vyapaarbuddy.enums.FileStockSourceType;
import com.vyapaarbuddy.enums.FileStockType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileStockEntryResponse {
    private Long id;
    private FileStockSourceType sourceType;
    private FileStockType fileType;
    private String originalFileName;
    private String extractedText;
    private FileStockEntryStatus status;
    private String errorMessage;
    private List<FileStockEntryItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
