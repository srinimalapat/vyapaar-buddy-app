import axiosClient from './axiosClient';
import { ReportResponse } from '../types/report';
import { ApiResponse } from '../types/common';

export const reportApi = {
  getDailySales: async (date?: string): Promise<ReportResponse> => {
    const response = await axiosClient.get<ApiResponse<ReportResponse>>('/v1/reports/daily-sales', {
      params: date ? { date } : undefined,
    });
    return response.data.data;
  },

  getMonthlySales: async (year: number, month: number): Promise<ReportResponse> => {
    const response = await axiosClient.get<ApiResponse<ReportResponse>>('/v1/reports/monthly-sales', {
      params: { year, month },
    });
    return response.data.data;
  },

  getCustomerCredit: async (): Promise<ReportResponse> => {
    const response = await axiosClient.get<ApiResponse<ReportResponse>>('/v1/reports/customer-credit');
    return response.data.data;
  },

  getInventoryLowStock: async (): Promise<ReportResponse> => {
    const response = await axiosClient.get<ApiResponse<ReportResponse>>('/v1/reports/inventory-low-stock');
    return response.data.data;
  },
};
