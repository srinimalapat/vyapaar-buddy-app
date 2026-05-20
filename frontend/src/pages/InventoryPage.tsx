import React from 'react';
import Button from '../components/common/Button';
import EmptyState from '../components/common/EmptyState';
import { Package } from 'lucide-react';

// TODO: Implement inventory list
// TODO: Add search and filter
// TODO: Add pagination

const InventoryPage: React.FC = () => {
  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">Inventory</h1>
        <Button>
          <Package size={20} className="mr-2" />
          Add Item
        </Button>
      </div>

      <EmptyState
        title="No inventory items yet"
        description="Add inventory items to track your stock"
        action={
          <Button>
            <Package size={20} className="mr-2" />
            Add Item
          </Button>
        }
      />
    </div>
  );
};

export default InventoryPage;
