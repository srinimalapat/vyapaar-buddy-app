import axiosClient from './axiosClient';
import { SaleRequest, SaleResponse } from '../types/sale';

export const saleApi = {
  getAll: async (page = 0, size = 10): Promise<SaleResponse[]> => {
    // TODO: Implement get all sales API call
    const response = await axiosClient.get<SaleResponse[]>('/sales', {
      params: { page, size },
    });
    return response.data;
  },

  getById: async (id: number): Promise<SaleResponse> => {
    // TODO: Implement get sale by ID API call
    const response = await axiosClient.get<SaleResponse>(`/sales/${id}`);
    return response.data;
  },

  create: async (data: SaleRequest): Promise<SaleResponse> => {
    // TODO: Implement create sale API call
    const response = await axiosClient.post<SaleResponse>('/sales', data);
    return response.data;
  },

  update: async (id: number, data: SaleRequest): Promise<SaleResponse> => {
    // TODO: Implement update sale API call
    const response = await axiosClient.put<SaleResponse>(`/sales/${id}`, data);
    return response.data;
  },

  delete: async (id: number): Promise<void> => {
    // TODO: Implement delete sale API call
    await axiosClient.delete(`/sales/${id}`);
  },
};
