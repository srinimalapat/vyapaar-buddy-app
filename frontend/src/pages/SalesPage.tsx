import React from 'react';
import Button from '../components/common/Button';
import EmptyState from '../components/common/EmptyState';
import { ShoppingCart } from 'lucide-react';

// TODO: Implement sales list
// TODO: Add search and filter
// TODO: Add pagination

const SalesPage: React.FC = () => {
  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">Sales</h1>
        <Button>
          <ShoppingCart size={20} className="mr-2" />
          New Sale
        </Button>
      </div>

      <EmptyState
        title="No sales yet"
        description="Record your first sale to get started"
        action={
          <Button>
            <ShoppingCart size={20} className="mr-2" />
            New Sale
          </Button>
        }
      />
    </div>
  );
};

export default SalesPage;
