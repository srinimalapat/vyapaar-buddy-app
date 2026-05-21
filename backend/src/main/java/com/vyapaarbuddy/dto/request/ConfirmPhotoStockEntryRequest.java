package com.vyapaarbuddy.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class ConfirmPhotoStockEntryRequest {
    private List<ConfirmPhotoStockEntryItemRequest> items;
    private boolean updateExistingItems = true;
}
