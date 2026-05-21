import axiosClient from './axiosClient';
import { CreditTransactionRequest, CreditTransactionResponse } from '../types/credit';
import { ApiResponse } from '../types/common';
import { CustomerResponse } from '../types/customer';

export const creditApi = {
  addCredit: async (data: CreditTransactionRequest): Promise<CreditTransactionResponse> => {
    const response = await axiosClient.post<ApiResponse<CreditTransactionResponse>>('/v1/credits', data);
    return response.data.data;
  },

  recordPayment: async (data: CreditTransactionRequest): Promise<CreditTransactionResponse> => {
    const response = await axiosClient.post<ApiResponse<CreditTransactionResponse>>('/v1/credits/payments', data);
    return response.data.data;
  },

  getCustomerHistory: async (customerId: number): Promise<CreditTransactionResponse[]> => {
    const response = await axiosClient.get<ApiResponse<CreditTransactionResponse[]>>(`/v1/credits/customers/${customerId}/history`);
    return response.data.data;
  },

  getPendingCustomers: async (): Promise<CustomerResponse[]> => {
    const response = await axiosClient.get<ApiResponse<CustomerResponse[]>>('/v1/credits/pending-customers');
    return response.data.data;
  },

  getTotalOutstanding: async () => {
    const response = await axiosClient.get<ApiResponse<any>>('/v1/credits/total-outstanding');
    return response.data.data;
  },
};
