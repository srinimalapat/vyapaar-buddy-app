export interface ReminderRequest {
  customerId: number;
  creditTransactionId: number;
  amount: number;
  dueDate?: string;
  scheduledDate?: string;
  channel?: string;
  status?: string;
  message?: string;
  notes?: string;
}

export interface ReminderResponse {
  id: number;
  customerId: number;
  creditTransactionId: number;
  businessId: number;
  amount: number;
  dueDate?: string;
  scheduledDate?: string;
  sentDate?: string;
  channel?: string;
  status: string;
  message?: string;
  notes?: string;
  retryCount: number;
  createdAt: string;
  updatedAt: string;
}
