import React, { useEffect, useState } from 'react';
import { DollarSign, ShoppingCart, Users, Package, TrendingUp, AlertCircle } from 'lucide-react';
import StatCard from '../components/dashboard/StatCard';
import Loader from '../components/common/Loader';
import { dashboardApi } from '../api/dashboardApi';
import { DashboardResponse } from '../types/dashboard';
import { formatCurrency } from '../utils/formatCurrency';

const DashboardPage: React.FC = () => {
  const [stats, setStats] = useState<DashboardResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    dashboardApi.getStats()
      .then(setStats)
      .catch(() => setError('Could not load dashboard stats'))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <Loader />;

  if (error || !stats) {
    return (
      <div className="space-y-6">
        <h1 className="text-2xl font-bold text-gray-900">Dashboard</h1>
        <div className="flex items-center gap-2 text-red-600 bg-red-50 border border-red-200 rounded-lg p-4">
          <AlertCircle size={18} />
          <span>{error || 'No data available'}</span>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">Dashboard</h1>
        <span className="text-sm text-gray-400">
          {new Date().toLocaleDateString('en-IN', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })}
        </span>
      </div>

      {/* Stat cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <StatCard
          title="Sales Today"
          value={formatCurrency(stats.totalSalesToday ?? 0)}
          icon={DollarSign}
        />
        <StatCard
          title="Transactions Today"
          value={String(stats.salesCountToday ?? 0)}
          icon={ShoppingCart}
        />
        <StatCard
          title="Total Customers"
          value={String(stats.totalCustomers ?? 0)}
          icon={Users}
        />
        <StatCard
          title="Inventory Items"
          value={String(stats.totalInventoryItems ?? 0)}
          icon={Package}
        />
      </div>

      {/* Second row */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
          <div className="flex items-center gap-2 mb-4">
            <TrendingUp size={20} className="text-orange-500" />
            <h2 className="text-lg font-semibold text-gray-900">Outstanding Credit (Udhaar)</h2>
          </div>
          {(stats.totalCreditOutstanding ?? 0) > 0 ? (
            <div>
              <p className="text-3xl font-bold text-orange-600">
                ₹{formatCurrency(stats.totalCreditOutstanding ?? 0)}
              </p>
              <p className="text-sm text-gray-500 mt-1">Total unpaid across all customers</p>
            </div>
          ) : (
            <p className="text-green-600 font-medium">All clear — no outstanding dues</p>
          )}
        </div>

        <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
          <div className="flex items-center gap-2 mb-4">
            <Package size={20} className="text-red-500" />
            <h2 className="text-lg font-semibold text-gray-900">Inventory Alerts</h2>
          </div>
          {(stats.lowStockItems ?? 0) > 0 ? (
            <div>
              <p className="text-3xl font-bold text-red-600">{stats.lowStockItems}</p>
              <p className="text-sm text-gray-500 mt-1">
                item{stats.lowStockItems !== 1 ? 's' : ''} running low on stock
              </p>
            </div>
          ) : (
            <p className="text-green-600 font-medium">All inventory levels are healthy</p>
          )}
        </div>
      </div>
    </div>
  );
};

export default DashboardPage;
