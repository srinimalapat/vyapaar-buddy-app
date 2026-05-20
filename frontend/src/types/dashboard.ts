export interface DashboardResponse {
  date: string;
  generatedAt: string;
  totalSalesToday: number;
  totalSalesThisMonth: number;
  salesCountToday: number;
  totalCustomers: number;
  activeCustomers: number;
  newCustomersThisMonth: number;
  totalCreditOutstanding: number;
  totalCreditOverdue: number;
  overdueCustomers: number;
  totalInventoryItems: number;
  lowStockItems: number;
  outOfStockItems: number;
}
