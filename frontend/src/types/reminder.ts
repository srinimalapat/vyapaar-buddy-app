export interface ReminderRequest {
  amountDue?: number;
  reminderDate?: string;
  message?: string;
  channel?: string;
}

export interface ReminderResponse {
  id: number;
  customerId?: number;
  customerName?: string;
  customerMobileNumber?: string;
  amountDue?: number;
  reminderDate?: string;
  message?: string;
  status: string;
  channel?: string;
  createdAt: string;
}
