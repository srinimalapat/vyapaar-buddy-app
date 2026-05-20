package com.vyapaarbuddy.service;

import com.vyapaarbuddy.dto.request.InventoryItemRequest;
import com.vyapaarbuddy.dto.response.InventoryItemResponse;
import com.vyapaarbuddy.entity.Business;
import com.vyapaarbuddy.entity.InventoryItem;
import com.vyapaarbuddy.enums.InventoryStatus;
import com.vyapaarbuddy.exception.BadRequestException;
import com.vyapaarbuddy.exception.ResourceNotFoundException;
import com.vyapaarbuddy.mapper.InventoryMapper;
import com.vyapaarbuddy.repository.InventoryItemRepository;
import com.vyapaarbuddy.security.CurrentUserService;
import com.vyapaarbuddy.service.impl.InventoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock private InventoryItemRepository inventoryItemRepository;
    @Mock private InventoryMapper inventoryMapper;
    @Mock private CurrentUserService currentUserService;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    private Business business;
    private InventoryItem item;
    private InventoryItemResponse itemResponse;

    @BeforeEach
    void setUp() {
        business = Business.builder().id(1L).name("Test Shop").build();
        item = InventoryItem.builder()
                .id(10L).name("Basmati Rice").category("Grains")
                .quantity(50).lowStockThreshold(10)
                .unitPrice(BigDecimal.valueOf(120))
                .status(InventoryStatus.ACTIVE).business(business).build();
        itemResponse = InventoryItemResponse.builder()
                .id(10L).itemName("Basmati Rice").quantityAvailable(50)
                .lowStockThreshold(10).unitPrice(BigDecimal.valueOf(120))
                .status(InventoryStatus.ACTIVE).lowStock(false).build();
    }

    @Test
    void addItem_success() {
        when(currentUserService.getCurrentBusiness()).thenReturn(business);
        when(inventoryItemRepository.existsByBusinessIdAndNameIgnoreCaseAndStatus(
                1L, "Basmati Rice", InventoryStatus.ACTIVE)).thenReturn(false);
        when(inventoryItemRepository.save(any())).thenReturn(item);
        when(inventoryMapper.toEntity(any())).thenReturn(item);
        when(inventoryMapper.toResponse(item)).thenReturn(itemResponse);

        InventoryItemRequest req = new InventoryItemRequest();
        req.setItemName("Basmati Rice");
        req.setQuantityAvailable(50);
        req.setLowStockThreshold(10);
        req.setUnitPrice(BigDecimal.valueOf(120));

        InventoryItemResponse response = inventoryService.addItem(req);

        assertNotNull(response);
        assertEquals("Basmati Rice", response.getItemName());
        verify(inventoryItemRepository).save(any());
    }

    @Test
    void addItem_duplicateName_throwsBadRequest() {
        when(currentUserService.getCurrentBusiness()).thenReturn(business);
        when(inventoryItemRepository.existsByBusinessIdAndNameIgnoreCaseAndStatus(
                1L, "Basmati Rice", InventoryStatus.ACTIVE)).thenReturn(true);

        InventoryItemRequest req = new InventoryItemRequest();
        req.setItemName("Basmati Rice");
        req.setQuantityAvailable(10);
        req.setLowStockThreshold(5);
        req.setUnitPrice(BigDecimal.valueOf(100));

        assertThrows(BadRequestException.class, () -> inventoryService.addItem(req));
    }

    @Test
    void getItemById_success() {
        when(currentUserService.getCurrentBusinessId()).thenReturn(1L);
        when(inventoryItemRepository.findByBusinessIdAndId(1L, 10L)).thenReturn(Optional.of(item));
        when(inventoryMapper.toResponse(item)).thenReturn(itemResponse);

        InventoryItemResponse response = inventoryService.getItemById(10L);

        assertNotNull(response);
        assertEquals(10L, response.getId());
    }

    @Test
    void getItemById_notFound_throwsResourceNotFound() {
        when(currentUserService.getCurrentBusinessId()).thenReturn(1L);
        when(inventoryItemRepository.findByBusinessIdAndId(1L, 99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> inventoryService.getItemById(99L));
    }

    @Test
    void updateStock_success() {
        when(currentUserService.getCurrentBusinessId()).thenReturn(1L);
        when(inventoryItemRepository.findByBusinessIdAndId(1L, 10L)).thenReturn(Optional.of(item));
        when(inventoryItemRepository.save(item)).thenReturn(item);
        when(inventoryMapper.toResponse(item)).thenReturn(itemResponse);

        InventoryItemResponse response = inventoryService.updateStock(10L, 30);

        assertEquals(30, item.getQuantity());
        assertNotNull(response);
    }

    @Test
    void deactivateItem_setsStatusInactive() {
        when(currentUserService.getCurrentBusinessId()).thenReturn(1L);
        when(inventoryItemRepository.findByBusinessIdAndId(1L, 10L)).thenReturn(Optional.of(item));
        when(inventoryItemRepository.save(item)).thenReturn(item);

        inventoryService.deactivateItem(10L);

        assertEquals(InventoryStatus.INACTIVE, item.getStatus());
        verify(inventoryItemRepository).save(item);
    }

    @Test
    void getLowStockItems_returnsMappedList() {
        when(currentUserService.getCurrentBusinessId()).thenReturn(1L);
        InventoryItem lowItem = InventoryItem.builder()
                .id(20L).name("Sugar").quantity(5).lowStockThreshold(10)
                .status(InventoryStatus.ACTIVE).business(business).build();
        InventoryItemResponse lowResponse = InventoryItemResponse.builder()
                .id(20L).itemName("Sugar").quantityAvailable(5).lowStockThreshold(10).lowStock(true).build();
        when(inventoryItemRepository.findActiveLowStockItems(1L)).thenReturn(List.of(lowItem));
        when(inventoryMapper.toResponse(lowItem)).thenReturn(lowResponse);

        List<InventoryItemResponse> result = inventoryService.getLowStockItems();

        assertEquals(1, result.size());
        assertTrue(result.get(0).isLowStock());
    }
}
