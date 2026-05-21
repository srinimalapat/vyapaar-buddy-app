import React, { useState } from 'react';
import { Camera, Upload, CheckCircle, XCircle, AlertTriangle, Edit2, Save } from 'lucide-react';
import toast from 'react-hot-toast';
import Button from '../components/common/Button';
import Badge from '../components/common/Badge';
import { photoStockApi } from '../api/photoStockApi';
import {
  PhotoStockEntry,
  PhotoStockEntryItem,
  ConfirmPhotoStockEntryItemRequest,
} from '../types/photoStock';

const STATUS_COLORS: Record<string, 'green' | 'orange' | 'red' | 'gray' | 'blue'> = {
  PENDING_REVIEW: 'orange',
  CONFIRMED: 'green',
  CANCELLED: 'gray',
  FAILED: 'red',
};

const CONFIDENCE_COLOR = (score: number): 'green' | 'orange' | 'red' => {
  if (score >= 0.85) return 'green';
  if (score >= 0.65) return 'orange';
  return 'red';
};

const PhotoStockEntryPage: React.FC = () => {
  const [file, setFile]             = useState<File | null>(null);
  const [mockText, setMockText]     = useState('');
  const [uploading, setUploading]   = useState(false);
  const [confirming, setConfirming] = useState(false);
  const [cancelling, setCancelling] = useState(false);
  const [entry, setEntry]           = useState<PhotoStockEntry | null>(null);
  const [editRows, setEditRows]     = useState<ConfirmPhotoStockEntryItemRequest[]>([]);
  const [editing, setEditing]       = useState(false);

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const f = e.target.files?.[0] ?? null;
    setFile(f);
  };

  const handleUpload = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!file) { toast.error('Please select an image file'); return; }
    setUploading(true);
    try {
      const result = await photoStockApi.upload(file, mockText || undefined);
      setEntry(result);
      setEditRows(toEditRows(result.items));
      setEditing(false);
      if (result.status === 'FAILED') {
        toast.error(result.errorMessage ?? 'Extraction failed');
      } else {
        toast.success(`Extracted ${result.items.length} item(s) — review and confirm`);
      }
    } catch (err: any) {
      toast.error(err?.response?.data?.message ?? 'Upload failed');
    } finally {
      setUploading(false);
    }
  };

  const handleConfirm = async () => {
    if (!entry) return;
    setConfirming(true);
    try {
      const updated = await photoStockApi.confirm(entry.id, {
        items: editRows,
        updateExistingItems: true,
      });
      setEntry(updated);
      toast.success('Inventory updated successfully!');
    } catch (err: any) {
      toast.error(err?.response?.data?.message ?? 'Confirm failed');
    } finally {
      setConfirming(false);
    }
  };

  const handleCancel = async () => {
    if (!entry) return;
    setCancelling(true);
    try {
      const updated = await photoStockApi.cancel(entry.id, 'Cancelled by user');
      setEntry(updated);
      toast('Entry cancelled');
    } catch (err: any) {
      toast.error(err?.response?.data?.message ?? 'Cancel failed');
    } finally {
      setCancelling(false);
    }
  };

  const updateRow = (idx: number, field: keyof ConfirmPhotoStockEntryItemRequest, value: string | number | null) => {
    setEditRows(prev => prev.map((r, i) => i === idx ? { ...r, [field]: value } : r));
  };

  const toEditRows = (items: PhotoStockEntryItem[]): ConfirmPhotoStockEntryItemRequest[] =>
    items.map(i => ({
      itemName:  i.itemName,
      quantity:  i.quantity ?? 0,
      unit:      i.unit ?? '',
      unitPrice: i.unitPrice,
      category:  i.category ?? 'General',
    }));

  const isPending = entry?.status === 'PENDING_REVIEW';

  return (
    <div className="space-y-6 max-w-5xl">
      {/* Header */}
      <div className="flex items-center gap-3">
        <Camera size={28} className="text-primary-600" />
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Photo Stock Entry</h1>
          <p className="text-sm text-gray-500 mt-0.5">
            Upload a supplier bill, stock sheet or handwritten list to auto-extract inventory items
          </p>
        </div>
      </div>

      {/* Upload form */}
      {!entry && (
        <form onSubmit={handleUpload} className="bg-white rounded-xl border border-gray-200 shadow-sm p-6 space-y-5">
          <h2 className="font-semibold text-gray-800">Upload Image</h2>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Image File <span className="text-red-500">*</span>
            </label>
            <div className="flex items-center gap-3">
              <input
                type="file"
                accept="image/*,.pdf"
                onChange={handleFileChange}
                className="block w-full text-sm text-gray-500 file:mr-3 file:py-2 file:px-4 file:rounded-lg file:border-0 file:text-sm file:font-medium file:bg-primary-50 file:text-primary-700 hover:file:bg-primary-100"
              />
            </div>
            {file && (
              <p className="text-xs text-gray-500 mt-1">
                {file.name} ({(file.size / 1024).toFixed(1)} KB)
              </p>
            )}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Mock Extracted Text{' '}
              <span className="text-gray-400 font-normal">(for local testing — paste item list here)</span>
            </label>
            <textarea
              value={mockText}
              onChange={e => setMockText(e.target.value)}
              rows={6}
              placeholder={`Rice 25kg 60\nSugar 10kg 45\nOil 5L 140\nTea powder 12 pcs 90\nDal 20kg ₹120`}
              className="w-full px-4 py-3 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-primary-500 font-mono"
            />
            <p className="text-xs text-gray-400 mt-1">
              Format: <code>ItemName Quantity Unit Price</code> — e.g. <code>Rice 25kg 60</code>
            </p>
          </div>

          <Button type="submit" isLoading={uploading} className="w-full">
            <Upload size={16} className="mr-2 inline" />
            Upload &amp; Extract Items
          </Button>
        </form>
      )}

      {/* Results */}
      {entry && (
        <div className="space-y-4">
          {/* Entry header */}
          <div className="bg-white rounded-xl border border-gray-200 shadow-sm p-5 flex items-start justify-between gap-4">
            <div className="space-y-1">
              <div className="flex items-center gap-2">
                <span className="font-semibold text-gray-800">Entry #{entry.id}</span>
                <Badge variant={STATUS_COLORS[entry.status] ?? 'gray'}>{entry.status.replace('_', ' ')}</Badge>
              </div>
              {entry.originalFileName && (
                <p className="text-sm text-gray-500">File: {entry.originalFileName}</p>
              )}
              {entry.errorMessage && (
                <p className="text-sm text-red-500">{entry.errorMessage}</p>
              )}
            </div>
            <button
              type="button"
              onClick={() => { setEntry(null); setFile(null); setMockText(''); }}
              className="text-sm text-primary-600 hover:underline whitespace-nowrap"
            >
              Upload New
            </button>
          </div>

          {/* Extracted text */}
          {entry.extractedText && (
            <div className="bg-gray-50 rounded-xl border border-gray-200 p-4">
              <p className="text-xs font-semibold text-gray-500 uppercase tracking-wide mb-2">Extracted Text</p>
              <pre className="text-sm text-gray-700 whitespace-pre-wrap font-mono">{entry.extractedText}</pre>
            </div>
          )}

          {/* Items table */}
          {entry.items.length > 0 && (
            <div className="bg-white rounded-xl border border-gray-200 shadow-sm overflow-hidden">
              <div className="flex items-center justify-between px-5 py-4 border-b border-gray-100">
                <h2 className="font-semibold text-gray-800">
                  Extracted Items ({entry.items.length})
                </h2>
                {isPending && (
                  <button
                    type="button"
                    onClick={() => setEditing(!editing)}
                    className="text-sm text-primary-600 hover:underline flex items-center gap-1"
                  >
                    <Edit2 size={13} />
                    {editing ? 'View Mode' : 'Edit Items'}
                  </button>
                )}
              </div>

              <div className="overflow-x-auto">
                <table className="w-full text-sm">
                  <thead className="bg-gray-50 border-b border-gray-100">
                    <tr>
                      <th className="text-left px-4 py-3 text-xs font-semibold text-gray-500 uppercase">Item Name</th>
                      <th className="text-right px-4 py-3 text-xs font-semibold text-gray-500 uppercase">Qty</th>
                      <th className="text-left px-4 py-3 text-xs font-semibold text-gray-500 uppercase">Unit</th>
                      <th className="text-right px-4 py-3 text-xs font-semibold text-gray-500 uppercase">Unit Price</th>
                      <th className="text-left px-4 py-3 text-xs font-semibold text-gray-500 uppercase">Category</th>
                      <th className="text-center px-4 py-3 text-xs font-semibold text-gray-500 uppercase">Confidence</th>
                      <th className="text-left px-4 py-3 text-xs font-semibold text-gray-500 uppercase">Warnings</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-50">
                    {editRows.map((row, idx) => (
                      <tr key={idx} className="hover:bg-gray-50">
                        <td className="px-4 py-3">
                          {editing
                            ? <input value={row.itemName} onChange={e => updateRow(idx, 'itemName', e.target.value)}
                                className="w-full border border-gray-300 rounded px-2 py-1 text-sm focus:outline-none focus:ring-1 focus:ring-primary-500" />
                            : <span className="font-medium text-gray-900">{row.itemName}</span>}
                        </td>
                        <td className="px-4 py-3 text-right">
                          {editing
                            ? <input type="number" value={row.quantity} onChange={e => updateRow(idx, 'quantity', Number(e.target.value))}
                                className="w-20 border border-gray-300 rounded px-2 py-1 text-sm text-right focus:outline-none focus:ring-1 focus:ring-primary-500" />
                            : row.quantity ?? '—'}
                        </td>
                        <td className="px-4 py-3">
                          {editing
                            ? <input value={row.unit} onChange={e => updateRow(idx, 'unit', e.target.value)}
                                className="w-20 border border-gray-300 rounded px-2 py-1 text-sm focus:outline-none focus:ring-1 focus:ring-primary-500" />
                            : row.unit || '—'}
                        </td>
                        <td className="px-4 py-3 text-right">
                          {editing
                            ? <input type="number" value={row.unitPrice ?? ''} onChange={e => updateRow(idx, 'unitPrice', e.target.value ? Number(e.target.value) : null)}
                                className="w-24 border border-gray-300 rounded px-2 py-1 text-sm text-right focus:outline-none focus:ring-1 focus:ring-primary-500" placeholder="₹" />
                            : row.unitPrice != null ? `₹${row.unitPrice}` : '—'}
                        </td>
                        <td className="px-4 py-3">
                          {editing
                            ? <input value={row.category} onChange={e => updateRow(idx, 'category', e.target.value)}
                                className="w-24 border border-gray-300 rounded px-2 py-1 text-sm focus:outline-none focus:ring-1 focus:ring-primary-500" />
                            : row.category}
                        </td>
                        <td className="px-4 py-3 text-center">
                          <Badge variant={CONFIDENCE_COLOR(entry.items[idx]?.confidenceScore ?? 0)}>
                            {((entry.items[idx]?.confidenceScore ?? 0) * 100).toFixed(0)}%
                          </Badge>
                        </td>
                        <td className="px-4 py-3">
                          {entry.items[idx]?.validationErrors
                            ? <span className="flex items-center gap-1 text-orange-600 text-xs">
                                <AlertTriangle size={12} />{entry.items[idx].validationErrors}
                              </span>
                            : <span className="text-green-600 text-xs">OK</span>}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              {/* Action buttons */}
              {isPending && (
                <div className="flex gap-3 p-4 border-t border-gray-100 bg-gray-50">
                  {editing && (
                    <Button variant="secondary" onClick={() => setEditing(false)}>
                      <Save size={14} className="mr-1 inline" /> Done Editing
                    </Button>
                  )}
                  <Button onClick={handleConfirm} isLoading={confirming}>
                    <CheckCircle size={14} className="mr-2 inline" />
                    Confirm &amp; Update Inventory
                  </Button>
                  <Button variant="secondary" onClick={handleCancel} isLoading={cancelling}>
                    <XCircle size={14} className="mr-2 inline" />
                    Cancel
                  </Button>
                </div>
              )}

              {entry.status === 'CONFIRMED' && (
                <div className="flex items-center gap-2 p-4 border-t border-gray-100 bg-green-50 text-green-700 text-sm font-medium">
                  <CheckCircle size={16} />
                  Inventory updated successfully for {entry.items.length} item(s)
                </div>
              )}
            </div>
          )}

          {entry.items.length === 0 && entry.status === 'PENDING_REVIEW' && (
            <div className="bg-yellow-50 border border-yellow-200 rounded-xl p-5 text-yellow-800 text-sm">
              <AlertTriangle size={16} className="inline mr-2" />
              No items could be extracted from the text. Please check the format and try again.
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default PhotoStockEntryPage;
