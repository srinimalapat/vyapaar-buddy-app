export interface InventoryItemRequest {
  name: string;
  sku?: string;
  description?: string;
  category?: string;
  unitPrice: number;
  quantity: number;
  lowStockThreshold: number;
  unit?: string;
  status?: string;
  supplier?: string;
  costPrice?: number;
}

export interface InventoryItemResponse {
  id: number;
  businessId: number;
  name: string;
  sku?: string;
  description?: string;
  category?: string;
  unitPrice: number;
  quantity: number;
  lowStockThreshold: number;
  unit?: string;
  status: string;
  supplier?: string;
  costPrice?: number;
  isLowStock: boolean;
  createdAt: string;
  updatedAt: string;
}
