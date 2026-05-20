import React from 'react';
import Card from '../components/common/Card';
import { MessageCircle } from 'lucide-react';

const WhatsAppPage: React.FC = () => {
  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-gray-900">WhatsApp Integration</h1>

      <Card>
        <div className="text-center py-8">
          <div className="p-4 bg-green-100 rounded-lg inline-block mb-4">
            <MessageCircle className="text-green-600" size={32} />
          </div>
          <h3 className="text-lg font-semibold text-gray-900 mb-2">WhatsApp Business</h3>
          <p className="text-gray-500">
            WhatsApp Cloud API integration is coming soon. You will be able to manage
            sales, credits, and customer queries directly from WhatsApp.
          </p>
        </div>
      </Card>
    </div>
  );
};

export default WhatsAppPage;
