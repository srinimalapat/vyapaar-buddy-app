import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Plus, IndianRupee, Calendar } from 'lucide-react';
import toast from 'react-hot-toast';
import Button from '../components/common/Button';
import EmptyState from '../components/common/EmptyState';
import Loader from '../components/common/Loader';
import Badge from '../components/common/Badge';
import { saleApi } from '../api/saleApi';
import { SaleResponse } from '../types/sale';
import { formatCurrency } from '../utils/formatCurrency';
import { formatDate } from '../utils/formatDate';

const saleTypeBadge = (type: string): 'green' | 'orange' | 'blue' | 'gray' => {
  const map: Record<string, 'green' | 'orange' | 'blue' | 'gray'> = {
    CASH: 'green', CREDIT: 'orange', UPI: 'blue', CARD: 'blue',
  };
  return map[type] || 'gray';
};

const SalesPage: React.FC = () => {
  const navigate = useNavigate();
  const [sales, setSales] = useState<SaleResponse[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    saleApi.getAll()
      .then(setSales)
      .catch(() => toast.error('Failed to load sales'))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <Loader />;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Sales</h1>
          <p className="text-sm text-gray-500 mt-1">{sales.length} total</p>
        </div>
        <Button onClick={() => navigate('/sales/new')}>
          <Plus size={18} className="mr-2 inline" /> New Sale
        </Button>
      </div>

      {sales.length === 0 ? (
        <EmptyState
          title="No sales yet"
          description="Record your first sale to start tracking your business"
          action={<Button onClick={() => navigate('/sales/new')}><Plus size={18} className="mr-2 inline" />New Sale</Button>}
        />
      ) : (
        <div className="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
          <table className="w-full">
            <thead className="bg-gray-50 border-b border-gray-200">
              <tr>
                <th className="text-left px-6 py-3 text-xs font-semibold text-gray-500 uppercase">Sale</th>
                <th className="text-left px-6 py-3 text-xs font-semibold text-gray-500 uppercase">Customer</th>
                <th className="text-left px-6 py-3 text-xs font-semibold text-gray-500 uppercase">Type</th>
                <th className="text-right px-6 py-3 text-xs font-semibold text-gray-500 uppercase">Total</th>
                <th className="text-right px-6 py-3 text-xs font-semibold text-gray-500 uppercase">Balance</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {sales.map(s => (
                <tr key={s.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4">
                    <div className="font-medium text-gray-900">#{s.id}</div>
                    <div className="text-sm text-gray-400 flex items-center gap-1">
                      <Calendar size={12} />{formatDate(s.saleDate)}
                    </div>
                  </td>
                  <td className="px-6 py-4 text-gray-700">{s.customerName || '—'}</td>
                  <td className="px-6 py-4"><Badge variant={saleTypeBadge(s.saleType)}>{s.saleType}</Badge></td>
                  <td className="px-6 py-4 text-right font-semibold">
                    <span className="inline-flex items-center gap-0.5"><IndianRupee size={13} />{formatCurrency(s.totalAmount)}</span>
                  </td>
                  <td className="px-6 py-4 text-right">
                    {s.balanceAmount > 0
                      ? <span className="text-orange-600 font-semibold inline-flex items-center gap-0.5"><IndianRupee size={13} />{formatCurrency(s.balanceAmount)}</span>
                      : <span className="text-green-600 text-sm font-medium">Paid</span>}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};

export default SalesPage;
