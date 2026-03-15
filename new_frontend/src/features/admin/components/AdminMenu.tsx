import React from 'react';
import { Link, useLocation } from 'react-router-dom';

const menuItems = [
  { label: 'Dashboard', path: '/admin' },
  { label: 'Products', path: '/admin/products' },
  { label: 'Orders', path: '/admin/orders' },
  { label: 'Users', path: '/admin/users' },
  { label: 'Analytics', path: '/admin/analytics' },
  { label: 'Settings', path: '/admin/settings' },
];

const AdminMenu: React.FC = () => {
  const location = useLocation();
  return (
    <nav className="bg-white shadow rounded p-4 mb-6">
      <ul className="flex flex-col gap-2">
        {menuItems.map(item => (
          <li key={item.path}>
            <Link
              to={item.path}
              className={`block px-3 py-2 rounded hover:bg-gray-100 ${location.pathname === item.path ? 'bg-blue-100 font-bold' : ''}`}
            >
              {item.label}
            </Link>
          </li>
        ))}
      </ul>
    </nav>
  );
};

export default AdminMenu;
