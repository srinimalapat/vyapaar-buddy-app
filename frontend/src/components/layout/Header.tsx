import { Bell, User, LogOut } from 'lucide-react';
import { useAuth } from '../../hooks/useAuth';
import { useNavigate } from 'react-router-dom';

const Header = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <header className="bg-white border-b border-gray-200 px-6 py-4">
      <div className="flex items-center justify-between">
        <div className="flex-1" />

        <div className="flex items-center gap-4">
          <button className="relative p-2 text-gray-600 hover:text-primary-600 transition-colors">
            <Bell size={20} />
          </button>

          <div className="flex items-center gap-3 pl-4 border-l border-gray-200">
            <div className="w-8 h-8 bg-primary-100 rounded-full flex items-center justify-center">
              <User size={18} className="text-primary-600" />
            </div>
            <div className="text-sm">
              <p className="font-medium text-gray-700">{user?.name || 'Business Owner'}</p>
              <p className="text-gray-400 text-xs">{user?.email || ''}</p>
            </div>
          </div>

          <button
            onClick={handleLogout}
            className="p-2 text-gray-400 hover:text-red-600 transition-colors"
            title="Logout"
          >
            <LogOut size={18} />
          </button>
        </div>
      </div>
    </header>
  );
};

export default Header;
