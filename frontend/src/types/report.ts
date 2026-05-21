import { SaleResponse } from './sale';
import { CustomerResponse } from './customer';
import { InventoryItemResponse } from './inventory';

export interface ReportResponse {
  reportType: string;
  date?: string;
  year?: number;
  month?: number;
  totalSales?: number;
  cashSales?: number;
  creditSales?: number;
  upiSales?: number;
  cardSales?: number;
  totalPaid?: number;
  totalBalance?: number;
  saleCount?: number;
  totalOutstandingCredit?: number;
  customersWithPendingCredit?: number;
  lowStockCount?: number;
  sales?: SaleResponse[];
  customers?: CustomerResponse[];
  inventoryItems?: InventoryItemResponse[];
}
