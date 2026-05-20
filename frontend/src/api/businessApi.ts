import axiosClient from './axiosClient';
import { BusinessRequest, BusinessResponse } from '../types/business';

interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data: T;
}

export const businessApi = {
  getMyBusiness: async (): Promise<BusinessResponse> => {
    const response = await axiosClient.get<ApiResponse<BusinessResponse>>('/v1/business/me');
    return response.data.data;
  },

  createBusiness: async (data: BusinessRequest): Promise<BusinessResponse> => {
    const response = await axiosClient.post<ApiResponse<BusinessResponse>>('/v1/business', data);
    return response.data.data;
  },

  updateBusiness: async (data: BusinessRequest): Promise<BusinessResponse> => {
    const response = await axiosClient.put<ApiResponse<BusinessResponse>>('/v1/business/me', data);
    return response.data.data;
  },
};
