import React from 'react';
import Button from '../components/common/Button';
import Card from '../components/common/Card';
import { BarChart3 } from 'lucide-react';

// TODO: Implement report generation
// TODO: Add date range picker
// TODO: Add export functionality

const ReportsPage: React.FC = () => {
  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-gray-900">Reports</h1>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <Card>
          <div className="text-center">
            <div className="p-4 bg-primary-100 rounded-lg inline-block mb-4">
              <BarChart3 className="text-primary-600" size={32} />
            </div>
            <h3 className="text-lg font-semibold text-gray-900 mb-2">Sales Report</h3>
            <p className="text-gray-500 mb-4">View sales analytics</p>
            <Button className="w-full">Generate</Button>
          </div>
        </Card>

        <Card>
          <div className="text-center">
            <div className="p-4 bg-green-100 rounded-lg inline-block mb-4">
              <BarChart3 className="text-green-600" size={32} />
            </div>
            <h3 className="text-lg font-semibold text-gray-900 mb-2">Credit Report</h3>
            <p className="text-gray-500 mb-4">View credit transactions</p>
            <Button className="w-full">Generate</Button>
          </div>
        </Card>

        <Card>
          <div className="text-center">
            <div className="p-4 bg-orange-100 rounded-lg inline-block mb-4">
              <BarChart3 className="text-orange-600" size={32} />
            </div>
            <h3 className="text-lg font-semibold text-gray-900 mb-2">Inventory Report</h3>
            <p className="text-gray-500 mb-4">View inventory status</p>
            <Button className="w-full">Generate</Button>
          </div>
        </Card>
      </div>
    </div>
  );
};

export default ReportsPage;
