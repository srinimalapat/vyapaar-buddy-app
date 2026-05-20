import axiosClient from './axiosClient';
import { MockWhatsAppRequest, MockCommandResponse } from '../types/mockWhatsApp';

export const mockWhatsAppApi = {
  parseMessage: async (data: MockWhatsAppRequest): Promise<MockCommandResponse> => {
    // TODO: Implement parse message API call
    const response = await axiosClient.post<MockCommandResponse>('/mock-whatsapp/parse', data);
    return response.data;
  },
};
