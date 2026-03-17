import { Routes, Route, useNavigate, Navigate } from 'react-router-dom'
import { ProtectedRoute } from './ProtectedRoute'
import { LoginForm } from '@/features/auth/components/LoginForm'
import { RegisterForm } from '@/features/auth/components/RegisterForm'
import { VerifyEmailPage } from '@/features/auth/components/VerifyEmailPage'
import {
  HomePage,
  AccountPage,
  CartPage,
  CheckoutPage,
  OrderDetailPage,
  ProductDetailPage,
} from '@/pages'
import {
  ProductsPage as AdminProductsPage,
  OrdersPage as AdminOrdersPage,
  UsersPage,
  AnalyticsPage
} from '@/features/admin'
import PaymentToolsPage from '@/pages/payment/PaymentToolsPage'
import PaymentReturnPage from '@/pages/payment/PaymentReturnPage'
import PaymentSuccessPage from '@/pages/payment/PaymentSuccessPage'
import PaymentPendingPage from '@/pages/payment/PaymentPendingPage'
import PaymentFailurePage from '@/pages/payment/PaymentFailurePage'
import PaymentCancelledPage from '@/pages/payment/PaymentCancelledPage'

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
          element={<LoginForm onLoginSuccess={() => navigate('/')} />} 
        />
        <Route 
          path="/register" 
          element={<RegisterForm />} 
        />
        <Route
          path="/verify-email"
          element={<VerifyEmailPage />}
        />

        {/* Products listing is merged into Home section */}
        <Route path="/products" element={<Navigate to="/" replace />} />
        <Route path="/products/:id" element={<ProductDetailPage />} />

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
              <CartPage />
            </ProtectedRoute>
          }
        />

        <Route
          path="/checkout"
          element={
            <ProtectedRoute requiredRole={['OWNER', 'CUSTOMER']}>
              <CheckoutPage />
            </ProtectedRoute>
          }
        />


        {/* Orders History */}
        <Route
          path="/orders"
          element={
            <ProtectedRoute requiredRole={['OWNER', 'CUSTOMER']}>
              <Navigate to="/account?tab=orders" replace />
            </ProtectedRoute>
          }
        />

        {/* My Account */}
        <Route
          path="/account"
          element={
            <ProtectedRoute requiredRole={['OWNER', 'CUSTOMER']}>
              <AccountPage />
            </ProtectedRoute>
          }
        />

        {/* Order Detail */}
        <Route
          path="/orders/:id"
          element={
            <ProtectedRoute requiredRole={['OWNER', 'CUSTOMER']}>
              <OrderDetailPage />
            </ProtectedRoute>
          }
        />

        {/* ========== ADMIN/OWNER ROUTES ========== */}
        {/* Requires: isAuthenticated = true && (user.role = 'OWNER' || user.role = 'ADMIN') */}
        <Route
          path="/admin"
          element={
            <ProtectedRoute requiredRole="OWNER">
              <AnalyticsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/products"
          element={
            <ProtectedRoute requiredRole="OWNER">
              <AdminProductsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/orders"
          element={
            <ProtectedRoute requiredRole="OWNER">
              <AdminOrdersPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/users"
          element={
            <ProtectedRoute requiredRole="OWNER">
              <UsersPage />
            </ProtectedRoute>
          }
        />

        {/* ========== PAYMENT ROUTES ========== */}
        <Route path="/payment/tools" element={
          <ProtectedRoute requiredRole="OWNER">
            <PaymentToolsPage />
          </ProtectedRoute>
        } />
        <Route path="/payment/return" element={<PaymentReturnPage />} />
        <Route path="/payment/success" element={<PaymentSuccessPage />} />
        <Route path="/payment/pending" element={<PaymentPendingPage />} />
        <Route path="/payment/failure" element={<PaymentFailurePage />} />
        <Route path="/payment/cancelled" element={<PaymentCancelledPage />} />

        {/* ========== 404 NOT FOUND ========== */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    )
  }
