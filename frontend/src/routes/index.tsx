import { createBrowserRouter } from 'react-router-dom';
import AppLayout from '../components/layout/AppLayout';
import LoginPage from '../pages/LoginPage';
import RegisterPage from '../pages/RegisterPage';
import BusinessSetupPage from '../pages/BusinessSetupPage';
import DashboardPage from '../pages/DashboardPage';
import CustomersPage from '../pages/CustomersPage';
import CustomerDetailPage from '../pages/CustomerDetailPage';
import SalesPage from '../pages/SalesPage';
import CreateSalePage from '../pages/CreateSalePage';
import CreditsPage from '../pages/CreditsPage';
import RemindersPage from '../pages/RemindersPage';
import InventoryPage from '../pages/InventoryPage';
import WhatsAppPage from '../pages/WhatsAppPage';
import PhotoStockEntryPage from '../pages/PhotoStockEntryPage';
import ReportsPage from '../pages/ReportsPage';

const router = createBrowserRouter([
  { path: '/login',    element: <LoginPage /> },
  { path: '/register', element: <RegisterPage /> },
  { path: '/setup',    element: <BusinessSetupPage /> },
  {
    path: '/',
    element: <AppLayout />,
    children: [
      { index: true,                    element: <DashboardPage /> },
      { path: 'dashboard',              element: <DashboardPage /> },
      { path: 'customers',              element: <CustomersPage /> },
      { path: 'customers/:id',          element: <CustomerDetailPage /> },
      { path: 'sales',                  element: <SalesPage /> },
      { path: 'sales/new',              element: <CreateSalePage /> },
      { path: 'credits',                element: <CreditsPage /> },
      { path: 'reminders',              element: <RemindersPage /> },
      { path: 'inventory',              element: <InventoryPage /> },
      { path: 'whatsapp',               element: <WhatsAppPage /> },
      { path: 'mock-whatsapp',          element: <WhatsAppPage /> },
      { path: 'photo-stock',            element: <PhotoStockEntryPage /> },
      { path: 'reports',                element: <ReportsPage /> },
    ],
  },
]);

export default router;
