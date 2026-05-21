import axiosClient from './axiosClient';
import { SaleRequest, SaleResponse } from '../types/sale';
import { ApiResponse } from '../types/common';

export const saleApi = {
  getAll: async (params?: { fromDate?: string; toDate?: string; customerId?: number; saleType?: string }): Promise<SaleResponse[]> => {
    const response = await axiosClient.get<ApiResponse<SaleResponse[]>>('/v1/sales', { params });
    return response.data.data;
  },

  getById: async (id: number): Promise<SaleResponse> => {
    const response = await axiosClient.get<ApiResponse<SaleResponse>>(`/v1/sales/${id}`);
    return response.data.data;
  },

  create: async (data: SaleRequest): Promise<SaleResponse> => {
    const response = await axiosClient.post<ApiResponse<SaleResponse>>('/v1/sales', data);
    return response.data.data;
  },

  getDailySummary: async (date: string) => {
    const response = await axiosClient.get<ApiResponse<any>>('/v1/sales/summary/daily', { params: { date } });
    return response.data.data;
  },

  getMonthlySummary: async (year: number, month: number) => {
    const response = await axiosClient.get<ApiResponse<any>>('/v1/sales/summary/monthly', { params: { year, month } });
    return response.data.data;
  },
};
