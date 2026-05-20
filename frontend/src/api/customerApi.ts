import axiosClient from './axiosClient';
import { CustomerRequest, CustomerResponse } from '../types/customer';

interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data: T;
}

export const customerApi = {
  getAll: async (status?: string): Promise<CustomerResponse[]> => {
    const response = await axiosClient.get<ApiResponse<CustomerResponse[]>>('/v1/customers', {
      params: status ? { status } : undefined,
    });
    return response.data.data;
  },

  search: async (query: string): Promise<CustomerResponse[]> => {
    const response = await axiosClient.get<ApiResponse<CustomerResponse[]>>('/v1/customers/search', {
      params: { query },
    });
    return response.data.data;
  },

  getById: async (id: number): Promise<CustomerResponse> => {
    const response = await axiosClient.get<ApiResponse<CustomerResponse>>(`/v1/customers/${id}`);
    return response.data.data;
  },

  create: async (data: CustomerRequest): Promise<CustomerResponse> => {
    const response = await axiosClient.post<ApiResponse<CustomerResponse>>('/v1/customers', data);
    return response.data.data;
  },

  update: async (id: number, data: CustomerRequest): Promise<CustomerResponse> => {
    const response = await axiosClient.put<ApiResponse<CustomerResponse>>(`/v1/customers/${id}`, data);
    return response.data.data;
  },

  deactivate: async (id: number): Promise<void> => {
    await axiosClient.delete(`/v1/customers/${id}`);
  },
};
