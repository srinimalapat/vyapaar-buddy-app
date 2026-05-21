import axiosClient from './axiosClient';
import { ApiResponse } from '../types/common';
import {
  FileStockEntry,
  FileStockEntryStatus,
  ConfirmFileStockEntryRequest,
} from '../types/fileStock';

export const fileStockApi = {
  upload: async (file: File, mockText?: string): Promise<FileStockEntry> => {
    const form = new FormData();
    form.append('file', file);
    if (mockText?.trim()) form.append('mockText', mockText.trim());
    const response = await axiosClient.post<ApiResponse<FileStockEntry>>(
      '/v1/file-stock/upload',
      form,
      { headers: { 'Content-Type': 'multipart/form-data' } }
    );
    return response.data.data;
  },

  getEntry: async (id: number): Promise<FileStockEntry> => {
    const response = await axiosClient.get<ApiResponse<FileStockEntry>>(`/v1/file-stock/${id}`);
    return response.data.data;
  },

  listEntries: async (status?: FileStockEntryStatus): Promise<FileStockEntry[]> => {
    const params = status ? { status } : undefined;
    const response = await axiosClient.get<ApiResponse<FileStockEntry[]>>('/v1/file-stock', { params });
    return response.data.data;
  },

  confirm: async (id: number, request?: ConfirmFileStockEntryRequest): Promise<FileStockEntry> => {
    const response = await axiosClient.post<ApiResponse<FileStockEntry>>(
      `/v1/file-stock/${id}/confirm`,
      request ?? {}
    );
    return response.data.data;
  },

  cancel: async (id: number, reason?: string): Promise<FileStockEntry> => {
    const response = await axiosClient.post<ApiResponse<FileStockEntry>>(
      `/v1/file-stock/${id}/cancel`,
      reason ? { reason } : {}
    );
    return response.data.data;
  },
};
