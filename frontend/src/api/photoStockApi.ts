import axiosClient from './axiosClient';
import { ApiResponse } from '../types/common';
import {
  PhotoStockEntry,
  PhotoStockEntryStatus,
  ConfirmPhotoStockEntryRequest,
} from '../types/photoStock';

export const photoStockApi = {
  upload: async (file: File, mockText?: string): Promise<PhotoStockEntry> => {
    const form = new FormData();
    form.append('file', file);
    if (mockText && mockText.trim()) {
      form.append('mockText', mockText.trim());
    }
    const response = await axiosClient.post<ApiResponse<PhotoStockEntry>>(
      '/v1/photo-stock/upload',
      form,
      { headers: { 'Content-Type': 'multipart/form-data' } }
    );
    return response.data.data;
  },

  getEntry: async (id: number): Promise<PhotoStockEntry> => {
    const response = await axiosClient.get<ApiResponse<PhotoStockEntry>>(`/v1/photo-stock/${id}`);
    return response.data.data;
  },

  listEntries: async (status?: PhotoStockEntryStatus): Promise<PhotoStockEntry[]> => {
    const params = status ? { status } : undefined;
    const response = await axiosClient.get<ApiResponse<PhotoStockEntry[]>>('/v1/photo-stock', { params });
    return response.data.data;
  },

  confirm: async (id: number, request?: ConfirmPhotoStockEntryRequest): Promise<PhotoStockEntry> => {
    const response = await axiosClient.post<ApiResponse<PhotoStockEntry>>(
      `/v1/photo-stock/${id}/confirm`,
      request ?? {}
    );
    return response.data.data;
  },

  cancel: async (id: number, reason?: string): Promise<PhotoStockEntry> => {
    const response = await axiosClient.post<ApiResponse<PhotoStockEntry>>(
      `/v1/photo-stock/${id}/cancel`,
      reason ? { reason } : {}
    );
    return response.data.data;
  },
};
