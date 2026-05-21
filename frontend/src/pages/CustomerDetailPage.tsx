import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { ArrowLeft, Phone, MapPin, IndianRupee } from "lucide-react";
import toast from "react-hot-toast";
import Loader from "../components/common/Loader";
import Badge from "../components/common/Badge";
import { customerApi } from "../api/customerApi";
import { creditApi } from "../api/creditApi";
import { CustomerResponse } from "../types/customer";
import { CreditTransactionResponse } from "../types/credit";
import { formatCurrency } from "../utils/formatCurrency";
import { formatDate } from "../utils/formatDate";

const CustomerDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [customer, setCustomer] = useState<CustomerResponse | null>(null);
  const [history, setHistory] = useState<CreditTransactionResponse[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!id) return;
    Promise.all([customerApi.getById(Number(id)), creditApi.getCustomerHistory(Number(id))])
      .then(([c, h]) => { setCustomer(c); setHistory(h); })
      .catch(() => toast.error("Failed to load customer"))
      .finally(() => setLoading(false));
  }, [id]);

  if (loading) return <Loader />;
  if (!customer) return <div className="text-center py-10 text-gray-500">Customer not found</div>;

  return (
    <div className="space-y-6 max-w-2xl">
      <div className="flex items-center gap-3">
        <button onClick={() => navigate("/customers")} className="p-2 text-gray-400 hover:text-gray-600 rounded-lg hover:bg-gray-100">
          <ArrowLeft size={20} />
        </button>
        <h1 className="text-2xl font-bold text-gray-900">{customer.customerName}</h1>
        <Badge variant={customer.status === "ACTIVE" ? "green" : "gray"}>{customer.status}</Badge>
      </div>

      <div className="bg-white rounded-xl border border-gray-200 shadow-sm p-6 space-y-3">
        {customer.mobileNumber && (
          <div className="flex items-center gap-2 text-gray-600"><Phone size={16} />{customer.mobileNumber}</div>
        )}
        {customer.address && (
          <div className="flex items-center gap-2 text-gray-600"><MapPin size={16} />{customer.address}</div>
        )}
        {customer.notes && <p className="text-sm text-gray-500">{customer.notes}</p>}
        <div className="pt-2 border-t border-gray-100">
          <p className="text-sm text-gray-500">Outstanding Balance</p>
          <p className="text-2xl font-bold text-orange-600 flex items-center gap-1 mt-1">
            <IndianRupee size={20} />{formatCurrency(customer.totalCreditAmount || 0)}
          </p>
        </div>
      </div>

      <div>
        <h2 className="text-lg font-semibold text-gray-900 mb-3">Credit History</h2>
        {history.length === 0 ? (
          <p className="text-gray-400 text-sm">No credit transactions yet</p>
        ) : (
          <div className="space-y-2">
            {history.map(tx => (
              <div key={tx.id} className="bg-white rounded-lg border border-gray-200 p-4 flex items-center justify-between">
                <div>
                  <div className="flex items-center gap-2">
                    <Badge variant={tx.transactionType === "PAYMENT_RECEIVED" ? "green" : "orange"}>
                      {tx.transactionType === "PAYMENT_RECEIVED" ? "Payment" : "Credit Given"}
                    </Badge>
                    <span className="text-sm text-gray-400">{formatDate(tx.transactionDate)}</span>
                  </div>
                  {tx.description && <p className="text-sm text-gray-500 mt-1">{tx.description}</p>}
                </div>
                <span className={`font-bold text-lg flex items-center gap-0.5 ${tx.transactionType === "PAYMENT_RECEIVED" ? "text-green-600" : "text-orange-600"}`}>
                  {tx.transactionType === "PAYMENT_RECEIVED" ? "-" : "+"}<IndianRupee size={14} />{formatCurrency(tx.amount)}
                </span>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default CustomerDetailPage;

