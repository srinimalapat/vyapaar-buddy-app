import { NavLink, useNavigate } from 'react-router-dom';
import { LayoutDashboard, Users, ShoppingCart, DollarSign, Bell, Package, BarChart3, MessageSquare, LogOut } from 'lucide-react';
import { useAuth } from '../../hooks/useAuth';

const menuItems = [
  { icon: LayoutDashboard, label: 'Dashboard',  path: '/dashboard' },
  { icon: Users,           label: 'Customers',  path: '/customers' },
  { icon: ShoppingCart,    label: 'Sales',       path: '/sales' },
  { icon: DollarSign,      label: 'Credits',     path: '/credits' },
  { icon: Bell,            label: 'Reminders',   path: '/reminders' },
  { icon: Package,         label: 'Inventory',   path: '/inventory' },
  { icon: MessageSquare,   label: 'WhatsApp',    path: '/whatsapp' },
  { icon: BarChart3,       label: 'Reports',     path: '/reports' },
];

const Sidebar = () => {
  const { logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <aside className="w-64 bg-white border-r border-gray-200 flex flex-col min-h-screen">
      {/* Brand */}
      <div className="p-6 shrink-0">
        <h1 className="text-2xl font-bold text-primary-600">Vyapaar Buddy</h1>
        <p className="text-sm text-gray-500 mt-1">MSME Assistant</p>
      </div>

      {/* Nav — grows to fill space */}
      <nav className="flex-1 overflow-y-auto mt-2">
        <ul className="space-y-1 px-4">
          {menuItems.map((item) => (
            <li key={item.path}>
              <NavLink
                to={item.path}
                className={({ isActive }) =>
                  `flex items-center gap-3 px-4 py-3 rounded-lg transition-colors text-sm font-medium ${
                    isActive
                      ? 'bg-primary-50 text-primary-600'
                      : 'text-gray-600 hover:bg-gray-50 hover:text-gray-900'
                  }`
                }
              >
                <item.icon size={18} />
                <span>{item.label}</span>
              </NavLink>
            </li>
          ))}
        </ul>
      </nav>

      {/* Logout — pinned to bottom inside flex column */}
      <div className="shrink-0 border-t border-gray-200 p-4">
        <button
          onClick={handleLogout}
          className="w-full flex items-center gap-3 px-4 py-3 text-sm font-medium text-gray-600 rounded-lg hover:bg-red-50 hover:text-red-600 transition-colors"
        >
          <LogOut size={18} />
          <span>Logout</span>
        </button>
      </div>
    </aside>
  );
};

export default Sidebar;
