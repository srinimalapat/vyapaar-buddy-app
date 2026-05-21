import axiosClient from './axiosClient';
import { InventoryItemRequest, InventoryItemResponse, StockUpdateRequest } from '../types/inventory';
import { ApiResponse } from '../types/common';

export const inventoryApi = {
  getAll: async (search?: string, status?: string): Promise<InventoryItemResponse[]> => {
    const response = await axiosClient.get<ApiResponse<InventoryItemResponse[]>>('/v1/inventory', {
      params: { search, status },
    });
    return response.data.data;
  },

  getLowStock: async (): Promise<InventoryItemResponse[]> => {
    const response = await axiosClient.get<ApiResponse<InventoryItemResponse[]>>('/v1/inventory/low-stock');
    return response.data.data;
  },

  getById: async (id: number): Promise<InventoryItemResponse> => {
    const response = await axiosClient.get<ApiResponse<InventoryItemResponse>>(`/v1/inventory/${id}`);
    return response.data.data;
  },

  create: async (data: InventoryItemRequest): Promise<InventoryItemResponse> => {
    const response = await axiosClient.post<ApiResponse<InventoryItemResponse>>('/v1/inventory', data);
    return response.data.data;
  },

  update: async (id: number, data: InventoryItemRequest): Promise<InventoryItemResponse> => {
    const response = await axiosClient.put<ApiResponse<InventoryItemResponse>>(`/v1/inventory/${id}`, data);
    return response.data.data;
  },

  updateStock: async (id: number, data: StockUpdateRequest): Promise<InventoryItemResponse> => {
    const response = await axiosClient.patch<ApiResponse<InventoryItemResponse>>(`/v1/inventory/${id}/stock`, data);
    return response.data.data;
  },

  deactivate: async (id: number): Promise<void> => {
    await axiosClient.delete(`/v1/inventory/${id}`);
  },
};
