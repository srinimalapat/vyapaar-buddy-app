export interface InventoryItemRequest {
  itemName: string;
  category?: string;
  quantityAvailable: number;
  lowStockThreshold: number;
  unitPrice: number;
  status?: string;
}

export interface InventoryItemResponse {
  id: number;
  itemName: string;
  category?: string;
  quantityAvailable: number;
  lowStockThreshold: number;
  unitPrice: number;
  status: string;
  lowStock: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface StockUpdateRequest {
  quantityAvailable: number;
}
