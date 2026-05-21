import React, { useEffect, useState } from 'react';
import { IndianRupee, Phone, TrendingUp } from 'lucide-react';
import toast from 'react-hot-toast';
import Loader from '../components/common/Loader';
import EmptyState from '../components/common/EmptyState';
import { creditApi } from '../api/creditApi';
import { CustomerResponse } from '../types/customer';
import { formatCurrency } from '../utils/formatCurrency';

const CreditsPage: React.FC = () => {
  const [customers, setCustomers] = useState<CustomerResponse[]>([]);
  const [total, setTotal] = useState<{ totalOutstandingCredit: number; customersWithPendingCredit: number } | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([creditApi.getPendingCustomers(), creditApi.getTotalOutstanding()])
      .then(([c, t]) => { setCustomers(c); setTotal(t); })
      .catch(() => toast.error('Failed to load credit data'))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <Loader />;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Credits (Udhaar)</h1>
          <p className="text-sm text-gray-500 mt-1">Customers with outstanding balance</p>
        </div>
      </div>

      {total && total.totalOutstandingCredit > 0 && (
        <div className="bg-orange-50 border border-orange-200 rounded-xl p-5 flex items-center gap-4">
          <div className="p-3 bg-orange-100 rounded-lg">
            <TrendingUp size={22} className="text-orange-600" />
          </div>
          <div>
            <p className="text-sm text-orange-700 font-medium">Total Outstanding Udhaar</p>
            <p className="text-2xl font-bold text-orange-700">
              ₹{formatCurrency(total.totalOutstandingCredit)}
            </p>
            <p className="text-xs text-orange-500">{total.customersWithPendingCredit} customers</p>
          </div>
        </div>
      )}

      {customers.length === 0 ? (
        <EmptyState title="No outstanding credits" description="All customers have cleared their dues" />
      ) : (
        <div className="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
          <table className="w-full">
            <thead className="bg-gray-50 border-b border-gray-200">
              <tr>
                <th className="text-left px-6 py-3 text-xs font-semibold text-gray-500 uppercase">Customer</th>
                <th className="text-left px-6 py-3 text-xs font-semibold text-gray-500 uppercase">Mobile</th>
                <th className="text-right px-6 py-3 text-xs font-semibold text-gray-500 uppercase">Outstanding</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {customers.map(c => (
                <tr key={c.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4 font-medium text-gray-900">{c.customerName}</td>
                  <td className="px-6 py-4">
                    {c.mobileNumber
                      ? <span className="flex items-center gap-1 text-gray-600"><Phone size={13} />{c.mobileNumber}</span>
                      : <span className="text-gray-400">—</span>}
                  </td>
                  <td className="px-6 py-4 text-right font-bold text-orange-600">
                    <span className="inline-flex items-center justify-end gap-0.5">
                      <IndianRupee size={13} />{formatCurrency(c.totalCreditAmount || 0)}
                    </span>
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

export default CreditsPage;
