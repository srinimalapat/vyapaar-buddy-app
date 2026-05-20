export interface CreditTransactionRequest {
  customerId: number;
  type: string;
  amount: number;
  transactionDate: string;
  dueDate?: string;
  description?: string;
  notes?: string;
}

export interface CreditTransactionResponse {
  id: number;
  customerId: number;
  businessId: number;
  type: string;
  amount: number;
  transactionDate: string;
  dueDate?: string;
  description?: string;
  notes?: string;
  isSettled: boolean;
  settledDate?: string;
  createdAt: string;
  updatedAt: string;
}
