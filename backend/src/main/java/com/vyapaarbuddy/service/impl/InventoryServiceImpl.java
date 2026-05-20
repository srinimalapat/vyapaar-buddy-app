package com.vyapaarbuddy.service.impl;

import com.vyapaarbuddy.dto.request.InventoryItemRequest;
import com.vyapaarbuddy.entity.InventoryItem;
import java.math.BigDecimal;
import com.vyapaarbuddy.dto.response.InventoryItemResponse;
import com.vyapaarbuddy.entity.Business;
import com.vyapaarbuddy.entity.InventoryItem;
import com.vyapaarbuddy.enums.InventoryStatus;
import com.vyapaarbuddy.exception.BadRequestException;
import com.vyapaarbuddy.exception.ResourceNotFoundException;
import com.vyapaarbuddy.mapper.InventoryMapper;
import com.vyapaarbuddy.repository.InventoryItemRepository;
import com.vyapaarbuddy.security.CurrentUserService;
import com.vyapaarbuddy.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryMapper inventoryMapper;
    private final CurrentUserService currentUserService;

    @Override
    @Transactional
    public InventoryItemResponse addItem(InventoryItemRequest request) {
        Business business = currentUserService.getCurrentBusiness();

        if (inventoryItemRepository.existsByBusinessIdAndNameIgnoreCaseAndStatus(
                business.getId(), request.getItemName(), InventoryStatus.ACTIVE)) {
            throw new BadRequestException("An active item with name '" + request.getItemName() + "' already exists");
        }

        InventoryItem item = inventoryMapper.toEntity(request);
        item.setBusiness(business);
        return inventoryMapper.toResponse(inventoryItemRepository.save(item));
    }

    @Override
    public InventoryItemResponse getItemById(Long id) {
        Long businessId = currentUserService.getCurrentBusinessId();
        InventoryItem item = inventoryItemRepository.findByBusinessIdAndId(businessId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found with id: " + id));
        return inventoryMapper.toResponse(item);
    }

    @Override
    public List<InventoryItemResponse> listItems(String search, String status) {
        Long businessId = currentUserService.getCurrentBusinessId();

        InventoryStatus statusFilter = null;
        if (status != null && !status.isBlank()) {
            try {
                statusFilter = InventoryStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid status value: " + status);
            }
        }

        List<InventoryItem> items;
        if (search != null && !search.isBlank() && statusFilter != null) {
            items = inventoryItemRepository.findByBusinessIdAndStatusAndNameContainingIgnoreCase(
                    businessId, statusFilter, search);
        } else if (search != null && !search.isBlank()) {
            items = inventoryItemRepository.findByBusinessIdAndNameContainingIgnoreCase(businessId, search);
        } else if (statusFilter != null) {
            items = inventoryItemRepository.findByBusinessIdAndStatus(businessId, statusFilter);
        } else {
            items = inventoryItemRepository.findByBusinessId(businessId);
        }

        return items.stream().map(inventoryMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public InventoryItemResponse updateItem(Long id, InventoryItemRequest request) {
        Long businessId = currentUserService.getCurrentBusinessId();
        InventoryItem item = inventoryItemRepository.findByBusinessIdAndId(businessId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found with id: " + id));

        boolean nameChanged = !item.getName().equalsIgnoreCase(request.getItemName());
        if (nameChanged && inventoryItemRepository.existsByBusinessIdAndNameIgnoreCaseAndStatus(
                businessId, request.getItemName(), InventoryStatus.ACTIVE)) {
            throw new BadRequestException("An active item with name '" + request.getItemName() + "' already exists");
        }

        inventoryMapper.updateEntity(item, request);
        return inventoryMapper.toResponse(inventoryItemRepository.save(item));
    }

    @Override
    @Transactional
    public InventoryItemResponse updateStock(Long id, Integer quantityAvailable) {
        Long businessId = currentUserService.getCurrentBusinessId();
        InventoryItem item = inventoryItemRepository.findByBusinessIdAndId(businessId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found with id: " + id));

        if (quantityAvailable < 0) {
            throw new BadRequestException("Stock quantity cannot be negative");
        }

        item.setQuantity(quantityAvailable);
        return inventoryMapper.toResponse(inventoryItemRepository.save(item));
    }

    @Override
    @Transactional
    public void deactivateItem(Long id) {
        Long businessId = currentUserService.getCurrentBusinessId();
        InventoryItem item = inventoryItemRepository.findByBusinessIdAndId(businessId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found with id: " + id));
        item.setStatus(InventoryStatus.INACTIVE);
        inventoryItemRepository.save(item);
    }

    @Override
    @Transactional
    public InventoryItemResponse addOrUpdateStock(String itemName, int quantity, BigDecimal unitPrice) {
        Long businessId = currentUserService.getCurrentBusinessId();
        return inventoryItemRepository
                .findByBusinessIdAndNameIgnoreCaseAndStatus(businessId, itemName, InventoryStatus.ACTIVE)
                .map(item -> {
                    item.setQuantity((item.getQuantity() != null ? item.getQuantity() : 0) + quantity);
                    if (unitPrice != null && unitPrice.compareTo(BigDecimal.ZERO) > 0) {
                        item.setUnitPrice(unitPrice);
                    }
                    return inventoryMapper.toResponse(inventoryItemRepository.save(item));
                })
                .orElseGet(() -> {
                    InventoryItem newItem = InventoryItem.builder()
                            .name(itemName)
                            .category("General")
                            .quantity(quantity)
                            .lowStockThreshold(5)
                            .unitPrice(unitPrice != null ? unitPrice : BigDecimal.ZERO)
                            .status(InventoryStatus.ACTIVE)
                            .business(currentUserService.getCurrentBusiness())
                            .build();
                    return inventoryMapper.toResponse(inventoryItemRepository.save(newItem));
                });
    }

    @Override
    public List<InventoryItemResponse> getLowStockItems() {
        Long businessId = currentUserService.getCurrentBusinessId();
        return inventoryItemRepository.findActiveLowStockItems(businessId)
                .stream().map(inventoryMapper::toResponse).toList();
    }
}
