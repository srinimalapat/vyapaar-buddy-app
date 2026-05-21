export interface MockWhatsAppRequest {
  message: string;
}

export interface MockCommandResponse {
  commandType: string;
  customerName?: string;
  itemName?: string;
  quantity?: number;
  amount?: number;
  paymentType?: string;
  rawMessage: string;
  confidenceScore: number;
  validationErrors: string[];
  executable: boolean;
  executed: boolean;
  executionMessage?: string;
  executionData?: any;
}
