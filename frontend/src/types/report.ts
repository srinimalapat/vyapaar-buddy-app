export interface ReportResponse {
  reportType: string;
  startDate?: string;
  endDate?: string;
  summary: Record<string, any>;
  data?: any[];
  pagination?: {
    page: number;
    size: number;
    total: number;
  };
}
