/**
 * React Router Configuration
 * Route definitions and layout structure
 */

import React from 'react';
import { createBrowserRouter, RouteObject } from 'react-router-dom';

// Auth
import { LoginPage, RegisterPage, ForgotPasswordPage, ResetPasswordPage, VerifyEmailPage } from '@auth/pages';
import { ProtectedRoute, PublicRoute } from '@auth/guards';

// Layouts
const RootLayout = React.lazy(() => import('@shared/layouts/RootLayout'));
const AuthLayout = React.lazy(() => import('@shared/layouts/AuthLayout'));

/**
 * Suspense Fallback Component
 */
const SuspenseFallback = () => (
  <div style={{ padding: '40px', textAlign: 'center' }}>
    <h2>Loading...</h2>
  </div>
);

/**
 * Error Fallback Component
 */
const ErrorFallback = () => (
  <div style={{ padding: '40px', textAlign: 'center' }}>
    <h1>Error Loading Page</h1>
    <p>There was an error loading this page. Please refresh.</p>
  </div>
);

/**
 * Auth routes
 */
const authRoutes: RouteObject[] = [
  {
    path: 'login',
    element: (
      <PublicRoute>
        <React.Suspense fallback={<div>Loading...</div>}>
          <AuthLayout>
            <LoginPage />
          </AuthLayout>
        </React.Suspense>
      </PublicRoute>
    ),
  },
  {
    path: 'register',
    element: (
      <PublicRoute>
        <React.Suspense fallback={<div>Loading...</div>}>
          <AuthLayout>
            <RegisterPage />
          </AuthLayout>
        </React.Suspense>
      </PublicRoute>
    ),
  },
  {
    path: 'forgot-password',
    element: (
      <PublicRoute>
        <React.Suspense fallback={<div>Loading...</div>}>
          <AuthLayout>
            <ForgotPasswordPage />
          </AuthLayout>
        </React.Suspense>
      </PublicRoute>
    ),
  },
  {
    path: 'reset-password',
    element: (
      <PublicRoute>
        <React.Suspense fallback={<div>Loading...</div>}>
          <AuthLayout>
            <ResetPasswordPage />
          </AuthLayout>
        </React.Suspense>
      </PublicRoute>
    ),
  },
  {
    path: 'email-verification',
    element: (
      <PublicRoute>
        <React.Suspense fallback={<div>Loading...</div>}>
          <AuthLayout>
            <VerifyEmailPage />
          </AuthLayout>
        </React.Suspense>
      </PublicRoute>
    ),
  },
];

/**
 * Product routes (placeholder)
 */
const productRoutes: RouteObject[] = [
  {
    path: 'products',
    element: (
      <ProtectedRoute>
        <React.Suspense fallback={<div>Loading...</div>}>
          <div>Product List Page (to be implemented)</div>
        </React.Suspense>
      </ProtectedRoute>
    ),
  },
  {
    path: 'products/:id',
    element: (
      <ProtectedRoute>
        <React.Suspense fallback={<div>Loading...</div>}>
          <div>Product Detail Page (to be implemented)</div>
        </React.Suspense>
      </ProtectedRoute>
    ),
  },
];

/**
 * Cart/Order routes (placeholder)
 */
const cartRoutes: RouteObject[] = [
  {
    path: 'cart',
    element: (
      <ProtectedRoute>
        <React.Suspense fallback={<div>Loading...</div>}>
          <div>Cart Page (to be implemented)</div>
        </React.Suspense>
      </ProtectedRoute>
    ),
  },
  {
    path: 'checkout',
    element: (
      <ProtectedRoute>
        <React.Suspense fallback={<div>Loading...</div>}>
          <div>Checkout Page (to be implemented)</div>
        </React.Suspense>
      </ProtectedRoute>
    ),
  },
  {
    path: 'orders',
    element: (
      <ProtectedRoute>
        <React.Suspense fallback={<div>Loading...</div>}>
          <div>Orders Page (to be implemented)</div>
        </React.Suspense>
      </ProtectedRoute>
    ),
  },
];

/**
 * Root routes
 */
const routes: RouteObject[] = [
  // Debug route (top-level)
  {
    path: 'debug',
    element: (
      <div style={{
        padding: '40px 20px',
        fontFamily: 'monospace',
        fontSize: '14px',
        whiteSpace: 'pre-wrap',
        maxWidth: '800px',
        margin: '0 auto',
      }}>
        <h1>Frontend Debug Info</h1>
        <p>Environment: {import.meta.env.MODE}</p>
        <p>API URL: {import.meta.env.VITE_API_URL || 'http://localhost:8080'}</p>
        <p>Build time: {new Date().toISOString()}</p>
        <hr />
        <h2>Auth Module Status</h2>
        <ul>
          <li>✓ AuthInitializer</li>
          <li>✓ LoginPage</li>
          <li>✓ RegisterPage</li>
          <li>✓ ForgotPasswordPage</li>
          <li>✓ ResetPasswordPage</li>
          <li>✓ VerifyEmailPage</li>
        </ul>
        <hr />
        <h2>Shared Module Status</h2>
        <ul>
          <li>✓ AuthLayout</li>
          <li>✓ RootLayout</li>
          <li>✓ useForm hook</li>
          <li>✓ Button component</li>
          <li>✓ Input component</li>
        </ul>
        <hr />
        <h2>Next Steps</h2>
        <ol>
          <li>Open browser console (F12) to see detailed logs</li>
          <li>Check the "?" button in bottom-right for auth state</li>
          <li>Try visiting /login to see the login page</li>
          <li>Check Network tab for API calls</li>
        </ol>
      </div>
    ),
  },

  // Auth routes (public, top-level)
  ...authRoutes,

  // Protected app routes (nested under RootLayout)
  {
    path: '/',
    element: (
      <React.Suspense fallback={<SuspenseFallback />}>
        <RootLayout />
      </React.Suspense>
    ),
    errorElement: <ErrorFallback />,
    children: [
      {
        index: true,
        element: (
          <ProtectedRoute>
            <React.Suspense fallback={<div>Loading...</div>}>
              <div>Dashboard Page (to be implemented)</div>
            </React.Suspense>
          </ProtectedRoute>
        ),
      },
      {
        path: 'dashboard',
        element: (
          <ProtectedRoute>
            <React.Suspense fallback={<div>Loading...</div>}>
              <div>Dashboard Page (to be implemented)</div>
            </React.Suspense>
          </ProtectedRoute>
        ),
      },
      ...productRoutes,
      ...cartRoutes,
      {
        path: 'unauthorized',
        element: (
          <div style={{ padding: '40px', textAlign: 'center' }}>
            <h1>Unauthorized</h1>
            <p>You don't have permission to access this page.</p>
            <a href="/">Go back home</a>
          </div>
        ),
      },
      {
        path: '*',
        element: (
          <div style={{ padding: '40px', textAlign: 'center' }}>
            <h1>404 - Page Not Found</h1>
            <p>The page you're looking for doesn't exist.</p>
            <a href="/">Go back home</a>
          </div>
        ),
      },
    ],
  },
];

/**
 * Create router
 */
export const router = createBrowserRouter(routes);

export default router;
