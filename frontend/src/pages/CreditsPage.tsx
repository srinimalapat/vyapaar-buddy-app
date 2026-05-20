import React from 'react';
import Button from '../components/common/Button';
import EmptyState from '../components/common/EmptyState';
import { DollarSign } from 'lucide-react';

// TODO: Implement credit transactions list
// TODO: Add search and filter
// TODO: Add pagination

const CreditsPage: React.FC = () => {
  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">Credits (Udhaar)</h1>
        <Button>
          <DollarSign size={20} className="mr-2" />
          Add Credit
        </Button>
      </div>

      <EmptyState
        title="No credit transactions yet"
        description="Record credit transactions to track outstanding payments"
        action={
          <Button>
            <DollarSign size={20} className="mr-2" />
            Add Credit
          </Button>
        }
      />
    </div>
  );
};

export default CreditsPage;
