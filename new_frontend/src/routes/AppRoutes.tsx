import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom'
import { ProtectedRoute } from './ProtectedRoute'

/** ============================================
 * ROUTE CONFIGURATION
 *
 * This file defines all routes in the application.
 * Routes are organized by accessibility level:
 * - Public: Anyone can access (Login, Register, Home)
 * - Protected: Requires authentication (Profile, Cart, Orders)
 * - Admin: Requires authentication + admin role (Dashboard, Users)
 *
 * Monitoring: Route changes logged to console with user info
 * ============================================ */

export function AppRoutes() {
  return (
    <Router>
      <Routes>
        {/* ========== PUBLIC ROUTES ========== */}

        {/* Landing / Home Page */}
        <Route path="/" element={<div className="p-4">Home Page (Coming Soon)</div>} />

        {/* Authentication Routes */}
        <Route path="/login" element={<div className="p-4">Login Page (Coming Soon)</div>} />
        <Route path="/register" element={<div className="p-4">Register Page (Coming Soon)</div>} />

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
              <div className="p-4">Orders (Coming Soon)</div>
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

        {/* ========== ADMIN ROUTES ========== */}
        {/* Requires: isAuthenticated = true && user.role = 'admin' */}

        {/* Admin Dashboard */}
        <Route
          path="/admin"
          element={
            <ProtectedRoute requiredRole="admin">
              <div className="p-4">Admin Dashboard (Coming Soon)</div>
            </ProtectedRoute>
          }
        />

        {/* Manage Users */}
        <Route
          path="/admin/users"
          element={
            <ProtectedRoute requiredRole="admin">
              <div className="p-4">Manage Users (Coming Soon)</div>
            </ProtectedRoute>
          }
        />

        {/* Manage Products */}
        <Route
          path="/admin/products"
          element={
            <ProtectedRoute requiredRole="admin">
              <div className="p-4">Manage Products (Coming Soon)</div>
            </ProtectedRoute>
          }
        />

        {/* View Analytics */}
        <Route
          path="/admin/analytics"
          element={
            <ProtectedRoute requiredRole="admin">
              <div className="p-4">Analytics Dashboard (Coming Soon)</div>
            </ProtectedRoute>
          }
        />

        {/* ========== 404 NOT FOUND ========== */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Router>
  )
}
