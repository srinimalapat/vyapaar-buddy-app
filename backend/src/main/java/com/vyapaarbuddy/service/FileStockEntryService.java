package com.vyapaarbuddy.service;

import com.vyapaarbuddy.dto.request.CancelFileStockEntryRequest;
import com.vyapaarbuddy.dto.request.ConfirmFileStockEntryRequest;
import com.vyapaarbuddy.dto.response.FileStockEntryResponse;
import com.vyapaarbuddy.enums.FileStockEntryStatus;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileStockEntryService {

    FileStockEntryResponse uploadAndExtract(MultipartFile file, String mockText);

    FileStockEntryResponse getEntry(Long id);

    List<FileStockEntryResponse> listEntries(FileStockEntryStatus status);

    FileStockEntryResponse confirmEntry(Long id, ConfirmFileStockEntryRequest request);

    FileStockEntryResponse cancelEntry(Long id, CancelFileStockEntryRequest request);
}
