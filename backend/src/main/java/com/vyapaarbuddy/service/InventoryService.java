package com.vyapaarbuddy.service;

import com.vyapaarbuddy.dto.request.InventoryItemRequest;
import com.vyapaarbuddy.dto.response.InventoryItemResponse;

import java.util.List;

public interface InventoryService {

    InventoryItemResponse addItem(InventoryItemRequest request);

    InventoryItemResponse getItemById(Long id);

    List<InventoryItemResponse> listItems(String search, String status);

    InventoryItemResponse updateItem(Long id, InventoryItemRequest request);

    InventoryItemResponse updateStock(Long id, Integer quantityAvailable);

    void deactivateItem(Long id);

    List<InventoryItemResponse> getLowStockItems();

    InventoryItemResponse addOrUpdateStock(String itemName, int quantity, java.math.BigDecimal unitPrice);
}
