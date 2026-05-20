export interface SaleItemRequest {
  inventoryItemId: number;
  quantity: number;
  unitPrice: number;
  discountAmount?: number;
  description?: string;
}

export interface SaleRequest {
  customerId: number;
  saleDate: string;
  type?: string;
  items: SaleItemRequest[];
  totalAmount: number;
  discountAmount?: number;
  taxAmount?: number;
  paidAmount?: number;
  notes?: string;
}

export interface SaleItemResponse {
  id: number;
  inventoryItemId: number;
  quantity: number;
  unitPrice: number;
  discountAmount?: number;
  totalPrice: number;
  description?: string;
  createdAt: string;
}

export interface SaleResponse {
  id: number;
  customerId: number;
  businessId: number;
  saleDate: string;
  type?: string;
  totalAmount: number;
  discountAmount?: number;
  taxAmount?: number;
  paidAmount?: number;
  notes?: string;
  items: SaleItemResponse[];
  createdAt: string;
  updatedAt: string;
}
