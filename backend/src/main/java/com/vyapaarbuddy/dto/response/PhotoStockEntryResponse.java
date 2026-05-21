package com.vyapaarbuddy.dto.response;

import com.vyapaarbuddy.enums.PhotoStockEntryStatus;
import com.vyapaarbuddy.enums.PhotoStockSourceType;
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
public class PhotoStockEntryResponse {
    private Long id;
    private PhotoStockSourceType sourceType;
    private String originalFileName;
    private String extractedText;
    private PhotoStockEntryStatus status;
    private String errorMessage;
    private List<PhotoStockEntryItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
