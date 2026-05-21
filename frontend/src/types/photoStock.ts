export type PhotoStockEntryStatus = 'PENDING_REVIEW' | 'CONFIRMED' | 'CANCELLED' | 'FAILED';
export type PhotoStockSourceType = 'LOCAL_UPLOAD' | 'WHATSAPP_MEDIA';

export interface PhotoStockEntryItem {
  id: number;
  itemName: string;
  quantity: number | null;
  unit: string | null;
  unitPrice: number | null;
  category: string;
  confidenceScore: number;
  validationErrors: string | null;
  createdAt: string;
}

export interface PhotoStockEntry {
  id: number;
  sourceType: PhotoStockSourceType;
  originalFileName: string | null;
  extractedText: string | null;
  status: PhotoStockEntryStatus;
  errorMessage: string | null;
  items: PhotoStockEntryItem[];
  createdAt: string;
  updatedAt: string | null;
}

export interface ConfirmPhotoStockEntryItemRequest {
  itemName: string;
  quantity: number;
  unit: string;
  unitPrice: number | null;
  category: string;
  lowStockThreshold?: number;
}

export interface ConfirmPhotoStockEntryRequest {
  items?: ConfirmPhotoStockEntryItemRequest[];
  updateExistingItems?: boolean;
}
