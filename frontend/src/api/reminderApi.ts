import axiosClient from './axiosClient';
import { ReminderRequest, ReminderResponse } from '../types/reminder';
import { ApiResponse } from '../types/common';

export const reminderApi = {
  getAll: async (customerId?: number, status?: string): Promise<ReminderResponse[]> => {
    const response = await axiosClient.get<ApiResponse<ReminderResponse[]>>('/v1/reminders', {
      params: { customerId, status },
    });
    return response.data.data;
  },

  getById: async (id: number): Promise<ReminderResponse> => {
    const response = await axiosClient.get<ApiResponse<ReminderResponse>>(`/v1/reminders/${id}`);
    return response.data.data;
  },

  generateForCustomer: async (customerId: number, data?: ReminderRequest): Promise<ReminderResponse> => {
    const response = await axiosClient.post<ApiResponse<ReminderResponse>>(`/v1/reminders/customer/${customerId}`, data || {});
    return response.data.data;
  },

  bulkGenerate: async (): Promise<ReminderResponse[]> => {
    const response = await axiosClient.post<ApiResponse<ReminderResponse[]>>('/v1/reminders/bulk');
    return response.data.data;
  },

  markSent: async (id: number): Promise<ReminderResponse> => {
    const response = await axiosClient.patch<ApiResponse<ReminderResponse>>(`/v1/reminders/${id}/sent`);
    return response.data.data;
  },

  cancel: async (id: number): Promise<ReminderResponse> => {
    const response = await axiosClient.patch<ApiResponse<ReminderResponse>>(`/v1/reminders/${id}/cancel`);
    return response.data.data;
  },
};
