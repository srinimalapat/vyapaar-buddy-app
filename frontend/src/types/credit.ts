export interface CreditTransactionRequest {
  customerId: number;
  transactionType: string;
  amount: number;
  transactionDate?: string;
  description?: string;
  allowOverPayment?: boolean;
}

export interface CreditTransactionResponse {
  id: number;
  customerId: number;
  customerName?: string;
  transactionType: string;
  amount: number;
  description?: string;
  transactionDate: string;
  createdAt: string;
}
