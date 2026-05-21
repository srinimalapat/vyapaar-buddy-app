import React, { useEffect, useState } from "react";
import { Bell, Send, X, Users } from "lucide-react";
import toast from "react-hot-toast";
import Button from "../components/common/Button";
import EmptyState from "../components/common/EmptyState";
import Loader from "../components/common/Loader";
import Badge from "../components/common/Badge";
import { reminderApi } from "../api/reminderApi";
import { ReminderResponse } from "../types/reminder";
import { formatCurrency } from "../utils/formatCurrency";
import { formatDate } from "../utils/formatDate";

const statusVariant = (s: string): "green" | "blue" | "orange" | "gray" | "red" => {
  const m: Record<string, "green" | "blue" | "orange" | "gray" | "red"> = {
    SENT: "green", PENDING: "orange", DELIVERED: "blue", CANCELLED: "gray", FAILED: "red",
  };
  return m[s] || "gray";
};

const RemindersPage: React.FC = () => {
  const [reminders, setReminders] = useState<ReminderResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [bulkLoading, setBulkLoading] = useState(false);

  const load = () => {
    setLoading(true);
    reminderApi.getAll()
      .then(setReminders)
      .catch(() => toast.error("Failed to load reminders"))
      .finally(() => setLoading(false));
  };

  useEffect(load, []);

  const handleBulkGenerate = async () => {
    setBulkLoading(true);
    try {
      const results = await reminderApi.bulkGenerate();
      toast.success(`Generated ${results.length} reminder(s)`);
      load();
    } catch (err: any) {
      toast.error(err?.response?.data?.message || "Failed to generate reminders");
    } finally { setBulkLoading(false); }
  };

  const handleMarkSent = async (id: number) => {
    try { await reminderApi.markSent(id); toast.success("Marked as sent"); load(); }
    catch { toast.error("Failed"); }
  };

  const handleCancel = async (id: number) => {
    try { await reminderApi.cancel(id); toast.success("Reminder cancelled"); load(); }
    catch { toast.error("Failed"); }
  };

  if (loading) return <Loader />;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Payment Reminders</h1>
          <p className="text-sm text-gray-500 mt-1">{reminders.length} total</p>
        </div>
        <Button onClick={handleBulkGenerate} isLoading={bulkLoading}>
          <Users size={18} className="mr-2 inline" />Bulk Generate
        </Button>
      </div>

      {reminders.length === 0 ? (
        <EmptyState title="No reminders yet" description="Generate reminders for customers with outstanding credit"
          action={<Button onClick={handleBulkGenerate} isLoading={bulkLoading}><Bell size={18} className="mr-2 inline" />Bulk Generate</Button>} />
      ) : (
        <div className="space-y-3">
          {reminders.map(r => (
            <div key={r.id} className="bg-white rounded-lg border border-gray-200 p-4 shadow-sm">
              <div className="flex items-start justify-between gap-4">
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-2 flex-wrap">
                    <span className="font-semibold text-gray-900">{r.customerName || "—"}</span>
                    {r.customerMobileNumber && <span className="text-sm text-gray-500">{r.customerMobileNumber}</span>}
                    <Badge variant={statusVariant(r.status)}>{r.status}</Badge>
                  </div>
                  {r.amountDue && <p className="text-sm text-orange-600 font-medium mt-1">Due: Rs.{formatCurrency(r.amountDue)}</p>}
                  {r.message && <p className="text-sm text-gray-500 mt-1 truncate">{r.message}</p>}
                  {r.reminderDate && <p className="text-xs text-gray-400 mt-1">{formatDate(r.reminderDate)}</p>}
                </div>
                {r.status === "PENDING" && (
                  <div className="flex gap-2 shrink-0">
                    <button onClick={() => handleMarkSent(r.id)} className="p-1.5 text-green-600 hover:bg-green-50 rounded" title="Mark as sent">
                      <Send size={15} />
                    </button>
                    <button onClick={() => handleCancel(r.id)} className="p-1.5 text-gray-400 hover:bg-gray-100 rounded" title="Cancel">
                      <X size={15} />
                    </button>
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default RemindersPage;
