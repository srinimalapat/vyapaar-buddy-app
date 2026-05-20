export interface CustomerRequest {
  customerName: string;
  mobileNumber?: string;
  address?: string;
  notes?: string;
  totalCreditAmount?: number;
}

export interface CustomerResponse {
  id: number;
  customerName: string;
  mobileNumber?: string;
  address?: string;
  notes?: string;
  totalCreditAmount: number;
  status: string;
  createdAt: string;
  updatedAt: string;
}
