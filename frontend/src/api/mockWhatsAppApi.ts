import axiosClient from './axiosClient';
import { MockWhatsAppRequest, MockCommandResponse } from '../types/mockWhatsApp';
import { ApiResponse } from '../types/common';

export const mockWhatsAppApi = {
  parse: async (message: string): Promise<MockCommandResponse> => {
    const payload: MockWhatsAppRequest = { message };
    const response = await axiosClient.post<ApiResponse<MockCommandResponse>>('/v1/mock-whatsapp/parse', payload);
    return response.data.data;
  },

  execute: async (message: string): Promise<MockCommandResponse> => {
    const payload: MockWhatsAppRequest = { message };
    const response = await axiosClient.post<ApiResponse<MockCommandResponse>>('/v1/mock-whatsapp/execute', payload);
    return response.data.data;
  },
};
