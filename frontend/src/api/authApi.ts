import axiosClient from './axiosClient';
import { LoginRequest, RegisterRequest, AuthResponse } from '../types/auth';

interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data: T;
}

export const authApi = {
  login: async (data: LoginRequest): Promise<AuthResponse> => {
    const response = await axiosClient.post<ApiResponse<AuthResponse>>('/auth/login', data);
    return response.data.data;
  },

  register: async (data: RegisterRequest): Promise<AuthResponse> => {
    const response = await axiosClient.post<ApiResponse<AuthResponse>>('/auth/register', data);
    return response.data.data;
  },
};
