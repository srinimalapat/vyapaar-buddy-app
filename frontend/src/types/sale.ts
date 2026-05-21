export interface SaleItemRequest {
  itemName: string;
  quantity: number;
  unitPrice: number;
}

export interface SaleRequest {
  saleType: string;
  customerId?: number;
  totalAmount: number;
  paidAmount: number;
  saleDate?: string;
  notes?: string;
  items: SaleItemRequest[];
}

export interface SaleItemResponse {
  id: number;
  itemName: string;
  quantity: number;
  unitPrice: number;
  totalPrice: number;
}

export interface SaleResponse {
  id: number;
  customerId?: number;
  customerName?: string;
  saleType: string;
  totalAmount: number;
  paidAmount: number;
  balanceAmount: number;
  saleDate: string;
  notes?: string;
  items: SaleItemResponse[];
  createdAt: string;
  updatedAt: string;
}
