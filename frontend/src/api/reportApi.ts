import axiosClient from './axiosClient';
import { ReportResponse } from '../types/report';

export const reportApi = {
  getSalesReport: async (startDate: string, endDate: string): Promise<ReportResponse> => {
    // TODO: Implement get sales report API call
    const response = await axiosClient.get<ReportResponse>('/reports/sales', {
      params: { startDate, endDate },
    });
    return response.data;
  },

  getCreditReport: async (startDate: string, endDate: string): Promise<ReportResponse> => {
    // TODO: Implement get credit report API call
    const response = await axiosClient.get<ReportResponse>('/reports/credit', {
      params: { startDate, endDate },
    });
    return response.data;
  },

  getInventoryReport: async (): Promise<ReportResponse> => {
    // TODO: Implement get inventory report API call
    const response = await axiosClient.get<ReportResponse>('/reports/inventory');
    return response.data;
  },
};
