import { Routes, Route, useNavigate, Navigate } from 'react-router-dom'
import { ProtectedRoute } from './ProtectedRoute'
import { LoginForm } from '@/features/auth/components/LoginForm'
import { RegisterForm } from '@/features/auth/components/RegisterForm'
import { VerifyEmailPage } from '@/features/auth/components/VerifyEmailPage'
import { HomePage, AccountPage, OrdersPage } from '@/pages'
import {
  AdminDashboard,
  DashboardPage,
  ProductsPage,
  OrdersPage as AdminOrdersPage,
  UsersPage,
  AnalyticsPage,
  SettingsPage
} from '@/features/admin'

/** ============================================
 * ROUTE CONFIGURATION
 *
 * This file defines all routes in the application.
 * Routes are organized by accessibility level:
 * - Public: Anyone can access (Home, Login, Register, Products)
 * - Protected: Requires authentication (Profile, Cart, Orders)
 * - Admin: Requires authentication + admin role (Dashboard, Users)
 *
 * Monitoring: Route changes logged to console with user info
 * ============================================ */

export function AppRoutes() {
  const navigate = useNavigate()
  return (
    <Routes>
        {/* ========== PUBLIC ROUTES ========== */}

        {/* Landing / Home Page - Default route when opening app */}
        <Route path="/" element={<HomePage />} />

        {/* Authentication Routes */}
        <Route 
          path="/login" 
          element={<LoginForm onLoginSuccess={() => navigate('/products')} />} 
        />
        <Route 
          path="/register" 
          element={<RegisterForm />} 
        />
        <Route
          path="/verify-email"
          element={<VerifyEmailPage />}
        />

        {/* Products Listing (Public) */}
        <Route path="/products" element={<div className="p-4">Products Page (Coming Soon)</div>} />
        <Route path="/products/:id" element={<div className="p-4">Product Detail (Coming Soon)</div>} />

        {/* ========== PROTECTED ROUTES ========== */}
        {/* Requires: isAuthenticated = true */}

        {/* User Profile */}
        <Route
          path="/profile"
          element={
            <ProtectedRoute>
              <div className="p-4">User Profile (Coming Soon)</div>
            </ProtectedRoute>
          }
        />

        {/* Shopping Cart */}
        <Route
          path="/cart"
          element={
            <ProtectedRoute>
              <div className="p-4">Shopping Cart (Coming Soon)</div>
            </ProtectedRoute>
          }
        />


        {/* Orders History */}
        <Route
          path="/orders"
          element={
            <ProtectedRoute>
              <OrdersPage />
            </ProtectedRoute>
          }
        />

        {/* My Account */}
        <Route
          path="/account"
          element={
            <ProtectedRoute>
              <AccountPage />
            </ProtectedRoute>
          }
        />

        {/* Order Detail */}
        <Route
          path="/orders/:id"
          element={
            <ProtectedRoute>
              <div className="p-4">Order Detail (Coming Soon)</div>
            </ProtectedRoute>
          }
        />

        {/* ========== ADMIN/OWNER ROUTES ========== */}
        {/* Requires: isAuthenticated = true && (user.role = 'OWNER' || user.role = 'ADMIN') */}
        <Route
          path="/admin"
          element={
            <ProtectedRoute requiredRole={["OWNER", "ADMIN"]}>
              <AdminDashboard />
            </ProtectedRoute>
          }
        >
          <Route index element={<DashboardPage />} />
          <Route path="products" element={<ProductsPage />} />
          <Route path="orders" element={<AdminOrdersPage />} />
          <Route path="users" element={<UsersPage />} />
          <Route path="analytics" element={<AnalyticsPage />} />
          <Route path="settings" element={<SettingsPage />} />
        </Route>

        {/* Manage Products */}
        <Route
          path="/admin/products"
          element={
            <ProtectedRoute requiredRole="ADMIN">
              <div className="p-4">Manage Products (Coming Soon)</div>
            </ProtectedRoute>
          }
        />

        {/* View Analytics */}
        <Route
          path="/admin/analytics"
          element={
            <ProtectedRoute requiredRole="ADMIN">
              <div className="p-4">Analytics Dashboard (Coming Soon)</div>
            </ProtectedRoute>
          }
        />

        {/* ========== 404 NOT FOUND ========== */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    )
  }
