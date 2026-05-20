export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
  phone?: string;
  businessName?: string;
  role?: string;
}

export interface AuthResponse {
  token: string;
  userId: number;
  email: string;
  name: string;
  type: string;
}
