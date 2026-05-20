export interface MockWhatsAppRequest {
  message: string;
  senderPhone?: string;
  customerId?: number;
}

export interface MockCommandResponse {
  commandType: string;
  originalMessage: string;
  isValid: boolean;
  errorMessage?: string;
  parsedData: Record<string, any>;
  suggestedResponse: string;
}
