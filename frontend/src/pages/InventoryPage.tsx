import React, { useCallback, useEffect, useState } from "react";
import { Plus, Search, Edit2, Trash2, X, AlertTriangle } from "lucide-react";
import toast from "react-hot-toast";
import Button from "../components/common/Button";
import Input from "../components/common/Input";
import EmptyState from "../components/common/EmptyState";
import Loader from "../components/common/Loader";
import Badge from "../components/common/Badge";
import { inventoryApi } from "../api/inventoryApi";
import { InventoryItemRequest, InventoryItemResponse } from "../types/inventory";
import { formatCurrency } from "../utils/formatCurrency";

const EMPTY: InventoryItemRequest = { itemName: "", category: "", quantityAvailable: 0, lowStockThreshold: 5, unitPrice: 0 };

const InventoryPage: React.FC = () => {
  const [items, setItems] = useState<InventoryItemResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState("");
  const [showModal, setShowModal] = useState(false);
  const [editItem, setEditItem] = useState<InventoryItemResponse | null>(null);
  const [form, setForm] = useState<InventoryItemRequest>(EMPTY);
  const [saving, setSaving] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    try { setItems(await inventoryApi.getAll(search || undefined)); }
    catch { toast.error("Failed to load inventory"); }
    finally { setLoading(false); }
  }, [search]);

  useEffect(() => { const t = setTimeout(load, 300); return () => clearTimeout(t); }, [load]);

  const openAdd = () => { setEditItem(null); setForm(EMPTY); setShowModal(true); };
  const openEdit = (item: InventoryItemResponse) => {
    setEditItem(item);
    setForm({ itemName: item.itemName, category: item.category || "", quantityAvailable: item.quantityAvailable, lowStockThreshold: item.lowStockThreshold, unitPrice: item.unitPrice });
    setShowModal(true);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!form.itemName.trim()) { toast.error("Item name is required"); return; }
    setSaving(true);
    try {
      if (editItem) { await inventoryApi.update(editItem.id, form); toast.success("Item updated"); }
      else { await inventoryApi.create(form); toast.success("Item added"); }
      setShowModal(false); load();
    } catch (err: any) { toast.error(err?.response?.data?.message || "Failed to save item"); }
    finally { setSaving(false); }
  };

  const handleDeactivate = async (item: InventoryItemResponse) => {
    if (!window.confirm(`Deactivate "${item.itemName}"?`)) return;
    try { await inventoryApi.deactivate(item.id); toast.success("Item deactivated"); load(); }
    catch { toast.error("Failed"); }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Inventory</h1>
          <p className="text-sm text-gray-500 mt-1">{items.length} items</p>
        </div>
        <Button onClick={openAdd}><Plus size={18} className="mr-2 inline" />Add Item</Button>
      </div>

      <div className="relative max-w-sm">
        <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={16} />
        <input value={search} onChange={e => setSearch(e.target.value)} placeholder="Search items..."
          className="w-full pl-9 pr-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 text-sm" />
      </div>

      {loading ? <Loader /> : items.length === 0 ? (
        <EmptyState title="No inventory items" description="Add items to track stock levels"
          action={<Button onClick={openAdd}><Plus size={18} className="mr-2 inline" />Add Item</Button>} />
      ) : (
        <div className="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
          <table className="w-full">
            <thead className="bg-gray-50 border-b border-gray-200">
              <tr>
                <th className="text-left px-6 py-3 text-xs font-semibold text-gray-500 uppercase">Item</th>
                <th className="text-left px-6 py-3 text-xs font-semibold text-gray-500 uppercase">Category</th>
                <th className="text-right px-6 py-3 text-xs font-semibold text-gray-500 uppercase">Stock</th>
                <th className="text-right px-6 py-3 text-xs font-semibold text-gray-500 uppercase">Price</th>
                <th className="text-left px-6 py-3 text-xs font-semibold text-gray-500 uppercase">Status</th>
                <th className="px-6 py-3" />
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {items.map(item => (
                <tr key={item.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4">
                    <div className="font-medium text-gray-900 flex items-center gap-2">
                      {item.itemName}
                      {item.lowStock && <AlertTriangle size={14} className="text-orange-500" />}
                    </div>
                  </td>
                  <td className="px-6 py-4 text-gray-500 text-sm">{item.category || "—"}</td>
                  <td className="px-6 py-4 text-right">
                    <span className={`font-semibold ${item.lowStock ? "text-orange-600" : "text-gray-900"}`}>{item.quantityAvailable}</span>
                    <span className="text-gray-400 text-xs ml-1">/ min {item.lowStockThreshold}</span>
                  </td>
                  <td className="px-6 py-4 text-right">Rs.{formatCurrency(item.unitPrice)}</td>
                  <td className="px-6 py-4"><Badge variant={item.status === "ACTIVE" ? "green" : "gray"}>{item.status}</Badge></td>
                  <td className="px-6 py-4">
                    <div className="flex items-center gap-2 justify-end">
                      <button onClick={() => openEdit(item)} className="p-1.5 text-gray-400 hover:text-primary-600 rounded"><Edit2 size={15} /></button>
                      {item.status === "ACTIVE" && <button onClick={() => handleDeactivate(item)} className="p-1.5 text-gray-400 hover:text-red-600 rounded"><Trash2 size={15} /></button>}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {showModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-md mx-4">
            <div className="flex items-center justify-between px-6 py-4 border-b">
              <h2 className="text-lg font-semibold">{editItem ? "Edit Item" : "Add Inventory Item"}</h2>
              <button onClick={() => setShowModal(false)} className="text-gray-400 hover:text-gray-600"><X size={20} /></button>
            </div>
            <form onSubmit={handleSubmit} className="p-6 space-y-4">
              <Input label="Item Name *" value={form.itemName} onChange={e => setForm(f => ({ ...f, itemName: e.target.value }))} placeholder="e.g. Basmati Rice 5kg" required />
              <Input label="Category" value={form.category || ""} onChange={e => setForm(f => ({ ...f, category: e.target.value }))} placeholder="e.g. Grains" />
              <div className="grid grid-cols-2 gap-4">
                <Input label="Qty" type="number" value={String(form.quantityAvailable)} onChange={e => setForm(f => ({ ...f, quantityAvailable: Number(e.target.value) }))} />
                <Input label="Min Stock" type="number" value={String(form.lowStockThreshold)} onChange={e => setForm(f => ({ ...f, lowStockThreshold: Number(e.target.value) }))} />
              </div>
              <Input label="Unit Price (Rs.)" type="number" value={String(form.unitPrice)} onChange={e => setForm(f => ({ ...f, unitPrice: Number(e.target.value) }))} />
              <div className="flex gap-3 pt-2">
                <Button type="button" variant="secondary" onClick={() => setShowModal(false)} className="flex-1">Cancel</Button>
                <Button type="submit" isLoading={saving} className="flex-1">{editItem ? "Save" : "Add Item"}</Button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default InventoryPage;
