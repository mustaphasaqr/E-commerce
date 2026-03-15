import React from 'react';
import AdminMenu from './AdminMenu';

const AdminDashboard: React.FC = () => (
  <div className="p-8">
    <AdminMenu />
    <div className="mt-6">
      <h1 className="text-3xl font-bold mb-6">Admin Dashboard</h1>
      <p>Welcome to the admin dashboard. Select a section from the menu.</p>
    </div>
  </div>
);

export default AdminDashboard;
