import React from 'react';
import Button from '../components/common/Button';
import EmptyState from '../components/common/EmptyState';
import { Bell } from 'lucide-react';

// TODO: Implement reminders list
// TODO: Add search and filter
// TODO: Add pagination

const RemindersPage: React.FC = () => {
  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">Reminders</h1>
        <Button>
          <Bell size={20} className="mr-2" />
          Create Reminder
        </Button>
      </div>

      <EmptyState
        title="No reminders yet"
        description="Create reminders to follow up on outstanding payments"
        action={
          <Button>
            <Bell size={20} className="mr-2" />
            Create Reminder
          </Button>
        }
      />
    </div>
  );
};

export default RemindersPage;
