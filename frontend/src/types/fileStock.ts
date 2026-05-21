export type FileStockEntryStatus = 'PENDING_REVIEW' | 'CONFIRMED' | 'CANCELLED' | 'FAILED';
export type FileStockSourceType  = 'LOCAL_UPLOAD' | 'WHATSAPP_MEDIA';
export type FileStockType        = 'IMAGE' | 'PDF' | 'EXCEL' | 'CSV' | 'WORD' | 'TEXT' | 'UNKNOWN';

export interface FileStockEntryItem {
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

export interface FileStockEntry {
  id: number;
  sourceType: FileStockSourceType;
  fileType: FileStockType;
  originalFileName: string | null;
  extractedText: string | null;
  status: FileStockEntryStatus;
  errorMessage: string | null;
  items: FileStockEntryItem[];
  createdAt: string;
  updatedAt: string | null;
}

export interface ConfirmFileStockEntryItemRequest {
  itemName: string;
  quantity: number;
  unit: string;
  unitPrice: number | null;
  category: string;
  lowStockThreshold?: number;
}

export interface ConfirmFileStockEntryRequest {
  items?: ConfirmFileStockEntryItemRequest[];
  updateExistingItems?: boolean;
}

export const FILE_TYPE_LABELS: Record<FileStockType, string> = {
  IMAGE:   'Image',
  PDF:     'PDF',
  EXCEL:   'Excel',
  CSV:     'CSV',
  WORD:    'Word',
  TEXT:    'Text',
  UNKNOWN: 'Unknown',
};

export const ACCEPTED_FILE_TYPES =
  'image/jpeg,image/jpg,image/png,image/webp,' +
  'application/pdf,' +
  'text/plain,text/csv,' +
  'application/vnd.ms-excel,' +
  'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,' +
  'application/msword,' +
  'application/vnd.openxmlformats-officedocument.wordprocessingml.document';
