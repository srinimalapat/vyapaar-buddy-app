import axiosClient from './axiosClient';
import { ReminderRequest, ReminderResponse } from '../types/reminder';

export const reminderApi = {
  getAll: async (page = 0, size = 10): Promise<ReminderResponse[]> => {
    // TODO: Implement get all reminders API call
    const response = await axiosClient.get<ReminderResponse[]>('/reminders', {
      params: { page, size },
    });
    return response.data;
  },

  getById: async (id: number): Promise<ReminderResponse> => {
    // TODO: Implement get reminder by ID API call
    const response = await axiosClient.get<ReminderResponse>(`/reminders/${id}`);
    return response.data;
  },

  create: async (data: ReminderRequest): Promise<ReminderResponse> => {
    // TODO: Implement create reminder API call
    const response = await axiosClient.post<ReminderResponse>('/reminders', data);
    return response.data;
  },

  update: async (id: number, data: ReminderRequest): Promise<ReminderResponse> => {
    // TODO: Implement update reminder API call
    const response = await axiosClient.put<ReminderResponse>(`/reminders/${id}`, data);
    return response.data;
  },

  send: async (id: number): Promise<ReminderResponse> => {
    // TODO: Implement send reminder API call
    const response = await axiosClient.post<ReminderResponse>(`/reminders/${id}/send`);
    return response.data;
  },
};
