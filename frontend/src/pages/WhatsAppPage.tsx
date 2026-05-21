import React, { useState } from "react";
import { MessageSquare, Send, Zap, CheckCircle, XCircle } from "lucide-react";
import toast from "react-hot-toast";
import Button from "../components/common/Button";
import Card from "../components/common/Card";
import Badge from "../components/common/Badge";
import { mockWhatsAppApi } from "../api/mockWhatsAppApi";
import { MockCommandResponse } from "../types/mockWhatsApp";

const EXAMPLES = [
  "Sale Ramesh rice 2kg 120 cash",
  "Udhaar Suresh 500",
  "Payment Ramesh 300",
  "Stock add sugar 10kg 45",
  "Report today",
];

const WhatsAppPage: React.FC = () => {
  const [message, setMessage] = useState("");
  const [result, setResult] = useState<MockCommandResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [mode, setMode] = useState<"parse" | "execute">("parse");

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!message.trim()) { toast.error("Enter a message first"); return; }
    setLoading(true);
    try {
      const res = mode === "parse"
        ? await mockWhatsAppApi.parse(message)
        : await mockWhatsAppApi.execute(message);
      setResult(res);
      if (mode === "execute" && res.executed) toast.success(res.executionMessage || "Executed!");
    } catch (err: any) {
      toast.error(err?.response?.data?.message || "Failed to process message");
    } finally { setLoading(false); }
  };

  return (
    <div className="space-y-6 max-w-2xl">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Mock WhatsApp</h1>
        <p className="text-sm text-gray-500 mt-1">Test WhatsApp-style commands without sending real messages</p>
      </div>

      <Card>
        <div className="flex gap-2 mb-4">
          <button onClick={() => setMode("parse")} className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${mode === "parse" ? "bg-primary-600 text-white" : "bg-gray-100 text-gray-600 hover:bg-gray-200"}`}>
            Parse Only
          </button>
          <button onClick={() => setMode("execute")} className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${mode === "execute" ? "bg-primary-600 text-white" : "bg-gray-100 text-gray-600 hover:bg-gray-200"}`}>
            Parse + Execute
          </button>
        </div>

        <form onSubmit={handleSubmit} className="space-y-3">
          <div className="flex gap-2">
            <input value={message} onChange={e => setMessage(e.target.value)}
              placeholder="e.g. Sale Ramesh rice 2kg 120 cash"
              className="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 text-sm" />
            <Button type="submit" isLoading={loading}>
              {mode === "execute" ? <Zap size={16} /> : <Send size={16} />}
            </Button>
          </div>
        </form>

        <div className="mt-3 flex flex-wrap gap-2">
          {EXAMPLES.map(ex => (
            <button key={ex} onClick={() => setMessage(ex)}
              className="text-xs px-2 py-1 bg-gray-100 text-gray-600 rounded hover:bg-gray-200 transition-colors">
              {ex}
            </button>
          ))}
        </div>
      </Card>

      {result && (
        <Card>
          <div className="flex items-center gap-2 mb-4">
            <MessageSquare size={18} className="text-primary-600" />
            <h2 className="font-semibold text-gray-900">Result</h2>
          </div>

          <div className="space-y-3">
            <div className="flex items-center gap-2 flex-wrap">
              <Badge variant="blue">{result.commandType}</Badge>
              <span className="text-sm text-gray-500">Confidence: {(result.confidenceScore * 100).toFixed(0)}%</span>
              {result.executable
                ? <span className="text-green-600 text-sm flex items-center gap-1"><CheckCircle size={14} />Executable</span>
                : <span className="text-red-500 text-sm flex items-center gap-1"><XCircle size={14} />Not executable</span>}
            </div>

            {result.customerName && <p className="text-sm"><span className="text-gray-500">Customer:</span> <span className="font-medium">{result.customerName}</span></p>}
            {result.itemName    && <p className="text-sm"><span className="text-gray-500">Item:</span> <span className="font-medium">{result.itemName}</span></p>}
            {result.quantity    && <p className="text-sm"><span className="text-gray-500">Qty:</span> <span className="font-medium">{result.quantity}</span></p>}
            {result.amount      && <p className="text-sm"><span className="text-gray-500">Amount:</span> <span className="font-medium">Rs.{result.amount}</span></p>}
            {result.paymentType && <p className="text-sm"><span className="text-gray-500">Payment:</span> <span className="font-medium">{result.paymentType}</span></p>}

            {result.validationErrors?.length > 0 && (
              <div className="bg-red-50 border border-red-200 rounded-lg p-3 text-sm text-red-700">
                {result.validationErrors.map((e, i) => <p key={i}>• {e}</p>)}
              </div>
            )}

            {result.executed && result.executionMessage && (
              <div className="bg-green-50 border border-green-200 rounded-lg p-3 text-sm text-green-700 flex items-center gap-2">
                <CheckCircle size={16} />{result.executionMessage}
              </div>
            )}
          </div>
        </Card>
      )}

      <Card>
        <h3 className="font-semibold text-gray-800 mb-3">Supported Commands</h3>
        <div className="space-y-2 text-sm text-gray-600">
          <p><strong>Sale:</strong> Sale [customer] [item] [qty] [amount] [cash/upi/card/credit]</p>
          <p><strong>Udhaar:</strong> Udhaar [customer] [amount]</p>
          <p><strong>Payment:</strong> Payment [customer] [amount]</p>
          <p><strong>Stock:</strong> Stock add [item] [qty] [price]</p>
          <p><strong>Report:</strong> Report today</p>
        </div>
      </Card>
    </div>
  );
};

export default WhatsAppPage;
