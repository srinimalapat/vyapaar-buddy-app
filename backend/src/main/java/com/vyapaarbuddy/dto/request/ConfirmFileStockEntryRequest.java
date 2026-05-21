package com.vyapaarbuddy.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class ConfirmFileStockEntryRequest {
    private List<ConfirmFileStockEntryItemRequest> items;
    private boolean updateExistingItems = true;
}
