import React, { useState } from "react";
import { BarChart3, FileText } from "lucide-react";
import toast from "react-hot-toast";
import Button from "../components/common/Button";
import Card from "../components/common/Card";
import Loader from "../components/common/Loader";
import { reportApi } from "../api/reportApi";
import { ReportResponse } from "../types/report";
import { formatCurrency } from "../utils/formatCurrency";

const today = new Date().toISOString().split("T")[0];
const year = new Date().getFullYear();
const month = new Date().getMonth() + 1;

const Stat: React.FC<{ label: string; value: string | number; sub?: string }> = ({ label, value, sub }) => (
  <div className="bg-gray-50 rounded-lg p-4">
    <p className="text-xs text-gray-500 mb-1">{label}</p>
    <p className="text-xl font-bold text-gray-900">{value}</p>
    {sub && <p className="text-xs text-gray-400 mt-0.5">{sub}</p>}
  </div>
);

const ReportsPage: React.FC = () => {
  const [report, setReport] = useState<ReportResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [date, setDate] = useState(today);
  const [yr, setYr] = useState(year);
  const [mo, setMo] = useState(month);

  const run = async (fn: () => Promise<ReportResponse>) => {
    setLoading(true);
    setReport(null);
    try { setReport(await fn()); }
    catch { toast.error("Failed to generate report"); }
    finally { setLoading(false); }
  };

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-gray-900">Reports</h1>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <Card>
          <h3 className="font-semibold text-gray-900 flex items-center gap-2 mb-3"><BarChart3 size={18} className="text-primary-600" />Daily Sales</h3>
          <input type="date" value={date} onChange={e => setDate(e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg mb-3 text-sm focus:ring-2 focus:ring-primary-500 focus:outline-none" />
          <Button className="w-full" onClick={() => run(() => reportApi.getDailySales(date))}>Generate</Button>
        </Card>

        <Card>
          <h3 className="font-semibold text-gray-900 flex items-center gap-2 mb-3"><BarChart3 size={18} className="text-blue-600" />Monthly Sales</h3>
          <div className="grid grid-cols-2 gap-2 mb-3">
            <input type="number" value={yr} onChange={e => setYr(Number(e.target.value))} placeholder="Year"
              className="px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-primary-500 focus:outline-none" />
            <input type="number" value={mo} onChange={e => setMo(Number(e.target.value))} min={1} max={12} placeholder="Month"
              className="px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-primary-500 focus:outline-none" />
          </div>
          <Button className="w-full" onClick={() => run(() => reportApi.getMonthlySales(yr, mo))}>Generate</Button>
        </Card>

        <Card>
          <h3 className="font-semibold text-gray-900 flex items-center gap-2 mb-3"><FileText size={18} className="text-orange-600" />Customer Credit</h3>
          <p className="text-sm text-gray-500 mb-3">All customers with outstanding udhaar balance</p>
          <Button className="w-full" onClick={() => run(reportApi.getCustomerCredit)}>Generate</Button>
        </Card>

        <Card>
          <h3 className="font-semibold text-gray-900 flex items-center gap-2 mb-3"><FileText size={18} className="text-red-600" />Low Stock</h3>
          <p className="text-sm text-gray-500 mb-3">Active inventory items below minimum threshold</p>
          <Button className="w-full" onClick={() => run(reportApi.getInventoryLowStock)}>Generate</Button>
        </Card>
      </div>

      {loading && <Loader />}

      {report && !loading && (
        <Card>
          <div className="flex items-center justify-between mb-4">
            <h2 className="font-semibold text-gray-900">{report.reportType?.replace(/_/g, " ")}</h2>
            <span className="text-xs text-gray-400">{report.date || (report.year ? `${report.year}-${report.month}` : "")}</span>
          </div>

          {(report.totalSales !== undefined || report.saleCount !== undefined) && (
            <div className="grid grid-cols-2 md:grid-cols-4 gap-3 mb-4">
              {report.totalSales !== undefined && <Stat label="Total Sales" value={`Rs.${formatCurrency(report.totalSales)}`} />}
              {report.cashSales  !== undefined && <Stat label="Cash" value={`Rs.${formatCurrency(report.cashSales)}`} />}
              {report.creditSales!== undefined && <Stat label="Credit" value={`Rs.${formatCurrency(report.creditSales)}`} />}
              {report.saleCount  !== undefined && <Stat label="Transactions" value={report.saleCount} />}
            </div>
          )}

          {report.totalOutstandingCredit !== undefined && (
            <div className="grid grid-cols-2 gap-3 mb-4">
              <Stat label="Total Outstanding" value={`Rs.${formatCurrency(report.totalOutstandingCredit)}`} />
              {report.customersWithPendingCredit !== undefined && <Stat label="Customers" value={report.customersWithPendingCredit} />}
            </div>
          )}

          {report.lowStockCount !== undefined && (
            <Stat label="Low Stock Items" value={report.lowStockCount} sub="Items below minimum threshold" />
          )}

          {report.customers && report.customers.length > 0 && (
            <div className="mt-4">
              <p className="text-sm font-medium text-gray-700 mb-2">Customers with pending credit:</p>
              <div className="space-y-1">
                {report.customers.map(c => (
                  <div key={c.id} className="flex justify-between text-sm py-1 border-b border-gray-100">
                    <span>{c.customerName}</span>
                    <span className="text-orange-600 font-medium">Rs.{formatCurrency(c.totalCreditAmount || 0)}</span>
                  </div>
                ))}
              </div>
            </div>
          )}

          {report.inventoryItems && report.inventoryItems.length > 0 && (
            <div className="mt-4">
              <p className="text-sm font-medium text-gray-700 mb-2">Low stock items:</p>
              <div className="space-y-1">
                {report.inventoryItems.map(i => (
                  <div key={i.id} className="flex justify-between text-sm py-1 border-b border-gray-100">
                    <span>{i.itemName}</span>
                    <span className="text-red-600 font-medium">{i.quantityAvailable} left</span>
                  </div>
                ))}
              </div>
            </div>
          )}
        </Card>
      )}
    </div>
  );
};

export default ReportsPage;
