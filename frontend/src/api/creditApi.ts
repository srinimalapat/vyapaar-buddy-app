import axiosClient from './axiosClient';
import { CreditTransactionRequest, CreditTransactionResponse } from '../types/credit';

export const creditApi = {
  getAll: async (customerId?: number): Promise<CreditTransactionResponse[]> => {
    // TODO: Implement get all credit transactions API call
    const url = customerId ? `/credits/customer/${customerId}` : '/credits';
    const response = await axiosClient.get<CreditTransactionResponse[]>(url);
    return response.data;
  },

  getById: async (id: number): Promise<CreditTransactionResponse> => {
    // TODO: Implement get credit transaction by ID API call
    const response = await axiosClient.get<CreditTransactionResponse>(`/credits/${id}`);
    return response.data;
  },

  create: async (data: CreditTransactionRequest): Promise<CreditTransactionResponse> => {
    // TODO: Implement create credit transaction API call
    const response = await axiosClient.post<CreditTransactionResponse>('/credits', data);
    return response.data;
  },

  settle: async (id: number): Promise<CreditTransactionResponse> => {
    // TODO: Implement settle credit API call
    const response = await axiosClient.put<CreditTransactionResponse>(`/credits/${id}/settle`);
    return response.data;
  },
};
