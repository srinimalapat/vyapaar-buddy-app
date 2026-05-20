import React, { useEffect, useState } from 'react';
import { Outlet, useNavigate } from 'react-router-dom';
import Sidebar from './Sidebar';
import Header from './Header';
import Loader from '../common/Loader';
import { businessApi } from '../../api/businessApi';
import { useAuth } from '../../hooks/useAuth';

const AppLayout: React.FC = () => {
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const [checking, setChecking] = useState(true);

  useEffect(() => {
    if (!isAuthenticated) { navigate('/login'); return; }

    businessApi.getMyBusiness()
      .then(() => setChecking(false))
      .catch((err) => {
        if (err?.response?.status === 404) {
          navigate('/setup');
        } else {
          // Any other error (network, 500) — still allow in, dashboard will show error
          setChecking(false);
        }
      });
  }, [isAuthenticated, navigate]);

  if (checking) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <Loader />
      </div>
    );
  }

  return (
    <div className="flex min-h-screen bg-gray-50">
      <Sidebar />
      <div className="flex-1 flex flex-col">
        <Header />
        <main className="flex-1 p-6 overflow-auto">
          <Outlet />
        </main>
      </div>
    </div>
  );
};

export default AppLayout;
