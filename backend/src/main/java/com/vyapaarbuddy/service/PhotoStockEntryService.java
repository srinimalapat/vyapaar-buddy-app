package com.vyapaarbuddy.service;

import com.vyapaarbuddy.dto.request.CancelPhotoStockEntryRequest;
import com.vyapaarbuddy.dto.request.ConfirmPhotoStockEntryRequest;
import com.vyapaarbuddy.dto.response.PhotoStockEntryResponse;
import com.vyapaarbuddy.enums.PhotoStockEntryStatus;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PhotoStockEntryService {

    PhotoStockEntryResponse uploadAndExtract(MultipartFile file, String mockText);

    PhotoStockEntryResponse getEntry(Long id);

    List<PhotoStockEntryResponse> listEntries(PhotoStockEntryStatus status);

    PhotoStockEntryResponse confirmEntry(Long id, ConfirmPhotoStockEntryRequest request);

    PhotoStockEntryResponse cancelEntry(Long id, CancelPhotoStockEntryRequest request);
}
