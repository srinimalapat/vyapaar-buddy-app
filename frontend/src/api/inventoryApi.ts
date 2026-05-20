import axiosClient from './axiosClient';
import { InventoryItemRequest, InventoryItemResponse } from '../types/inventory';

export const inventoryApi = {
  getAll: async (page = 0, size = 10): Promise<InventoryItemResponse[]> => {
    // TODO: Implement get all inventory items API call
    const response = await axiosClient.get<InventoryItemResponse[]>('/inventory', {
      params: { page, size },
    });
    return response.data;
  },

  getLowStock: async (): Promise<InventoryItemResponse[]> => {
    // TODO: Implement get low stock items API call
    const response = await axiosClient.get<InventoryItemResponse[]>('/inventory/low-stock');
    return response.data;
  },

  getById: async (id: number): Promise<InventoryItemResponse> => {
    // TODO: Implement get inventory item by ID API call
    const response = await axiosClient.get<InventoryItemResponse>(`/inventory/${id}`);
    return response.data;
  },

  create: async (data: InventoryItemRequest): Promise<InventoryItemResponse> => {
    // TODO: Implement create inventory item API call
    const response = await axiosClient.post<InventoryItemResponse>('/inventory', data);
    return response.data;
  },

  update: async (id: number, data: InventoryItemRequest): Promise<InventoryItemResponse> => {
    // TODO: Implement update inventory item API call
    const response = await axiosClient.put<InventoryItemResponse>(`/inventory/${id}`, data);
    return response.data;
  },

  delete: async (id: number): Promise<void> => {
    // TODO: Implement delete inventory item API call
    await axiosClient.delete(`/inventory/${id}`);
  },
};
