import axiosClient from './axiosClient';
import { DashboardResponse } from '../types/dashboard';

interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data: T;
}

export const dashboardApi = {
  getStats: async (): Promise<DashboardResponse> => {
    const response = await axiosClient.get<ApiResponse<DashboardResponse>>('/v1/dashboard');
    return response.data.data;
  },
};
