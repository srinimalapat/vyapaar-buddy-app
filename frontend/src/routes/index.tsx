import { createBrowserRouter } from 'react-router-dom';
import AppLayout from '../components/layout/AppLayout';
import LoginPage from '../pages/LoginPage';
import RegisterPage from '../pages/RegisterPage';
import BusinessSetupPage from '../pages/BusinessSetupPage';
import DashboardPage from '../pages/DashboardPage';
import CustomersPage from '../pages/CustomersPage';
import SalesPage from '../pages/SalesPage';
import CreditsPage from '../pages/CreditsPage';
import RemindersPage from '../pages/RemindersPage';
import InventoryPage from '../pages/InventoryPage';
import WhatsAppPage from '../pages/WhatsAppPage';
import ReportsPage from '../pages/ReportsPage';

const router = createBrowserRouter([
  { path: '/login',    element: <LoginPage /> },
  { path: '/register', element: <RegisterPage /> },
  { path: '/setup',    element: <BusinessSetupPage /> },
  {
    path: '/',
    element: <AppLayout />,
    children: [
      { index: true,             element: <DashboardPage /> },
      { path: 'dashboard',       element: <DashboardPage /> },
      { path: 'customers',       element: <CustomersPage /> },
      { path: 'sales',           element: <SalesPage /> },
      { path: 'credits',         element: <CreditsPage /> },
      { path: 'reminders',       element: <RemindersPage /> },
      { path: 'inventory',       element: <InventoryPage /> },
      { path: 'whatsapp',        element: <WhatsAppPage /> },
      { path: 'reports',         element: <ReportsPage /> },
    ],
  },
]);

export default router;
