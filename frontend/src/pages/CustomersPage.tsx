import React, { useEffect, useState, useCallback } from 'react';
import { UserPlus, Search, Edit2, Trash2, X, Phone, MapPin, IndianRupee } from 'lucide-react';
import Button from '../components/common/Button';
import Input from '../components/common/Input';
import EmptyState from '../components/common/EmptyState';
import Loader from '../components/common/Loader';
import { customerApi } from '../api/customerApi';
import { CustomerResponse, CustomerRequest } from '../types/customer';
import { formatCurrency } from '../utils/formatCurrency';

const EMPTY_FORM: CustomerRequest = {
  customerName: '',
  mobileNumber: '',
  address: '',
  notes: '',
};

const CustomersPage: React.FC = () => {
  const [customers, setCustomers] = useState<CustomerResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [editCustomer, setEditCustomer] = useState<CustomerResponse | null>(null);
  const [form, setForm] = useState<CustomerRequest>(EMPTY_FORM);
  const [saving, setSaving] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);

  const loadCustomers = useCallback(async (query?: string) => {
    setLoading(true);
    setError(null);
    try {
      const data = query && query.trim()
        ? await customerApi.search(query.trim())
        : await customerApi.getAll();
      setCustomers(data);
    } catch {
      setError('Failed to load customers. Please try again.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { loadCustomers(); }, [loadCustomers]);

  useEffect(() => {
    const timer = setTimeout(() => loadCustomers(searchQuery), 300);
    return () => clearTimeout(timer);
  }, [searchQuery, loadCustomers]);

  const openAdd = () => {
    setEditCustomer(null);
    setForm(EMPTY_FORM);
    setFormError(null);
    setShowModal(true);
  };

  const openEdit = (c: CustomerResponse) => {
    setEditCustomer(c);
    setForm({ customerName: c.customerName, mobileNumber: c.mobileNumber || '', address: c.address || '', notes: c.notes || '' });
    setFormError(null);
    setShowModal(true);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!form.customerName.trim()) { setFormError('Customer name is required'); return; }
    setSaving(true);
    setFormError(null);
    try {
      if (editCustomer) {
        await customerApi.update(editCustomer.id, form);
      } else {
        await customerApi.create(form);
      }
      setShowModal(false);
      loadCustomers(searchQuery);
    } catch (err: any) {
      setFormError(err?.response?.data?.message || 'Failed to save customer');
    } finally {
      setSaving(false);
    }
  };

  const handleDeactivate = async (c: CustomerResponse) => {
    if (!window.confirm(`Deactivate ${c.customerName}?`)) return;
    try {
      await customerApi.deactivate(c.id);
      loadCustomers(searchQuery);
    } catch {
      alert('Failed to deactivate customer');
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Customers</h1>
          <p className="text-sm text-gray-500 mt-1">{customers.length} total</p>
        </div>
        <Button onClick={openAdd}>
          <UserPlus size={18} className="mr-2 inline" />
          Add Customer
        </Button>
      </div>

      {/* Search */}
      <div className="relative max-w-md">
        <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={18} />
        <input
          type="text"
          placeholder="Search by name or mobile..."
          value={searchQuery}
          onChange={e => setSearchQuery(e.target.value)}
          className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
        />
      </div>

      {/* Content */}
      {loading ? (
        <Loader />
      ) : error ? (
        <div className="text-center py-10 text-red-600">{error}</div>
      ) : customers.length === 0 ? (
        <EmptyState
          title="No customers found"
          description={searchQuery ? 'Try a different search term' : 'Add your first customer to get started'}
          action={!searchQuery ? <Button onClick={openAdd}><UserPlus size={18} className="mr-2 inline" />Add Customer</Button> : undefined}
        />
      ) : (
        <div className="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
          <table className="w-full">
            <thead className="bg-gray-50 border-b border-gray-200">
              <tr>
                <th className="text-left px-6 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">Customer</th>
                <th className="text-left px-6 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">Mobile</th>
                <th className="text-left px-6 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">Credit (Udhaar)</th>
                <th className="text-left px-6 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">Status</th>
                <th className="px-6 py-3" />
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {customers.map(c => (
                <tr key={c.id} className="hover:bg-gray-50 transition-colors">
                  <td className="px-6 py-4">
                    <div className="font-medium text-gray-900">{c.customerName}</div>
                    {c.address && <div className="text-sm text-gray-500 flex items-center gap-1 mt-0.5"><MapPin size={12} />{c.address}</div>}
                  </td>
                  <td className="px-6 py-4">
                    {c.mobileNumber ? (
                      <span className="flex items-center gap-1 text-gray-700"><Phone size={14} />{c.mobileNumber}</span>
                    ) : (
                      <span className="text-gray-400 text-sm">—</span>
                    )}
                  </td>
                  <td className="px-6 py-4">
                    {c.totalCreditAmount > 0 ? (
                      <span className="font-semibold text-orange-600 flex items-center gap-0.5">
                        <IndianRupee size={14} />{formatCurrency(c.totalCreditAmount)}
                      </span>
                    ) : (
                      <span className="text-green-600 text-sm font-medium">No dues</span>
                    )}
                  </td>
                  <td className="px-6 py-4">
                    <span className={`inline-flex px-2 py-0.5 rounded-full text-xs font-semibold ${
                      c.status === 'ACTIVE' ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-500'
                    }`}>{c.status}</span>
                  </td>
                  <td className="px-6 py-4">
                    <div className="flex items-center gap-2 justify-end">
                      <button onClick={() => openEdit(c)} className="p-1.5 text-gray-400 hover:text-primary-600 rounded transition-colors">
                        <Edit2 size={16} />
                      </button>
                      {c.status === 'ACTIVE' && (
                        <button onClick={() => handleDeactivate(c)} className="p-1.5 text-gray-400 hover:text-red-600 rounded transition-colors">
                          <Trash2 size={16} />
                        </button>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Modal */}
      {showModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-md mx-4">
            <div className="flex items-center justify-between px-6 py-4 border-b border-gray-200">
              <h2 className="text-lg font-semibold text-gray-900">
                {editCustomer ? 'Edit Customer' : 'Add Customer'}
              </h2>
              <button onClick={() => setShowModal(false)} className="text-gray-400 hover:text-gray-600">
                <X size={20} />
              </button>
            </div>
            <form onSubmit={handleSubmit} className="p-6 space-y-4">
              {formError && (
                <div className="bg-red-50 border border-red-200 text-red-700 rounded-lg px-4 py-2 text-sm">{formError}</div>
              )}
              <Input
                label="Customer Name *"
                value={form.customerName}
                onChange={e => setForm(f => ({ ...f, customerName: e.target.value }))}
                placeholder="e.g. Ramesh Kumar"
                required
              />
              <Input
                label="Mobile Number"
                value={form.mobileNumber || ''}
                onChange={e => setForm(f => ({ ...f, mobileNumber: e.target.value }))}
                placeholder="10-digit mobile number"
                maxLength={10}
              />
              <Input
                label="Address"
                value={form.address || ''}
                onChange={e => setForm(f => ({ ...f, address: e.target.value }))}
                placeholder="e.g. 45 MG Road, Pune"
              />
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Notes</label>
                <textarea
                  value={form.notes || ''}
                  onChange={e => setForm(f => ({ ...f, notes: e.target.value }))}
                  placeholder="Optional notes"
                  rows={2}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 resize-none"
                />
              </div>
              <div className="flex gap-3 pt-2">
                <Button type="button" variant="secondary" onClick={() => setShowModal(false)} className="flex-1">
                  Cancel
                </Button>
                <Button type="submit" isLoading={saving} className="flex-1">
                  {editCustomer ? 'Save Changes' : 'Add Customer'}
                </Button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default CustomersPage;
