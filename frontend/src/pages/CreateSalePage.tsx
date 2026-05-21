import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { ArrowLeft, Plus, Trash2 } from "lucide-react";
import toast from "react-hot-toast";
import Button from "../components/common/Button";
import Input from "../components/common/Input";
import { saleApi } from "../api/saleApi";
import { customerApi } from "../api/customerApi";
import { SaleRequest, SaleItemRequest } from "../types/sale";
import { CustomerResponse } from "../types/customer";

const SALE_TYPES = ["CASH", "UPI", "CARD", "CREDIT"];

const CreateSalePage: React.FC = () => {
  const navigate = useNavigate();
  const [customers, setCustomers] = useState<CustomerResponse[]>([]);
  const [saving, setSaving] = useState(false);
  const [form, setForm] = useState<SaleRequest>({
    saleType: "CASH", totalAmount: 0, paidAmount: 0, items: [],
  });
  const [items, setItems] = useState<SaleItemRequest[]>([{ itemName: "", quantity: 1, unitPrice: 0 }]);

  useEffect(() => {
    customerApi.getAll().then(setCustomers).catch(() => {});
  }, []);

  const computedTotal = items.reduce((s, i) => s + i.quantity * i.unitPrice, 0);

  const setItem = (idx: number, field: keyof SaleItemRequest, value: string | number) =>
    setItems(prev => prev.map((it, i) => i === idx ? { ...it, [field]: value } : it));

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const validItems = items.filter(i => i.itemName.trim() && i.quantity > 0 && i.unitPrice >= 0);
    if (validItems.length === 0) { toast.error("Add at least one item"); return; }
    setSaving(true);
    try {
      const isCredit = form.saleType === "CREDIT";
      await saleApi.create({
        ...form,
        totalAmount: computedTotal,
        paidAmount: isCredit ? 0 : computedTotal,
        items: validItems,
      });
      toast.success("Sale recorded!");
      navigate("/sales");
    } catch (err: any) {
      toast.error(err?.response?.data?.message || "Failed to create sale");
    } finally { setSaving(false); }
  };

  return (
    <div className="max-w-2xl space-y-6">
      <div className="flex items-center gap-3">
        <button onClick={() => navigate("/sales")} className="p-2 text-gray-400 hover:text-gray-600 rounded-lg hover:bg-gray-100">
          <ArrowLeft size={20} />
        </button>
        <h1 className="text-2xl font-bold text-gray-900">New Sale</h1>
      </div>

      <form onSubmit={handleSubmit} className="space-y-6">
        <div className="bg-white rounded-xl border border-gray-200 shadow-sm p-6 space-y-4">
          <h2 className="font-semibold text-gray-800">Sale Details</h2>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Payment Type</label>
            <select value={form.saleType} onChange={e => setForm(f => ({ ...f, saleType: e.target.value }))}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500">
              {SALE_TYPES.map(t => <option key={t} value={t}>{t}</option>)}
            </select>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Customer (optional for cash)</label>
            <select value={form.customerId || ""} onChange={e => setForm(f => ({ ...f, customerId: Number(e.target.value) || undefined }))}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500">
              <option value="">— No Customer —</option>
              {customers.map(c => <option key={c.id} value={c.id}>{c.customerName}</option>)}
            </select>
          </div>
          <Input label="Notes (optional)" value={form.notes || ""} onChange={e => setForm(f => ({ ...f, notes: e.target.value }))} placeholder="Optional note" />
        </div>

        <div className="bg-white rounded-xl border border-gray-200 shadow-sm p-6 space-y-3">
          <div className="flex items-center justify-between">
            <h2 className="font-semibold text-gray-800">Items</h2>
            <button type="button" onClick={() => setItems(p => [...p, { itemName: "", quantity: 1, unitPrice: 0 }])}
              className="text-primary-600 text-sm flex items-center gap-1 hover:underline">
              <Plus size={14} /> Add item
            </button>
          </div>
          {items.map((item, idx) => (
            <div key={idx} className="grid grid-cols-12 gap-2 items-end">
              <div className="col-span-5">
                <Input label={idx === 0 ? "Item Name" : ""} value={item.itemName}
                  onChange={e => setItem(idx, "itemName", e.target.value)} placeholder="e.g. Rice 5kg" />
              </div>
              <div className="col-span-2">
                <Input label={idx === 0 ? "Qty" : ""} type="number" value={String(item.quantity)}
                  onChange={e => setItem(idx, "quantity", Number(e.target.value))} />
              </div>
              <div className="col-span-3">
                <Input label={idx === 0 ? "Unit Price" : ""} type="number" value={String(item.unitPrice)}
                  onChange={e => setItem(idx, "unitPrice", Number(e.target.value))} />
              </div>
              <div className="col-span-2 pb-2 text-right">
                <span className="text-sm font-medium text-gray-700">Rs.{(item.quantity * item.unitPrice).toFixed(2)}</span>
                {items.length > 1 && (
                  <button type="button" onClick={() => setItems(p => p.filter((_, i) => i !== idx))}
                    className="ml-2 p-1 text-gray-400 hover:text-red-500"><Trash2 size={14} /></button>
                )}
              </div>
            </div>
          ))}
          <div className="flex justify-between items-center pt-2 border-t border-gray-100 font-semibold">
            <span>Total</span>
            <span className="text-lg">Rs.{computedTotal.toFixed(2)}</span>
          </div>
        </div>

        <div className="flex gap-3">
          <Button type="button" variant="secondary" onClick={() => navigate("/sales")} className="flex-1">Cancel</Button>
          <Button type="submit" isLoading={saving} className="flex-1">Record Sale</Button>
        </div>
      </form>
    </div>
  );
};

export default CreateSalePage;
