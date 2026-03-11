/**
 * ProtectedRoute Guard
 * Route wrapper that checks authentication before allowing access
 */

import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAppSelector } from '@store/hooks';

interface ProtectedRouteProps {
  children: React.ReactNode;
  requiredRole?: ('CUSTOMER' | 'ADMIN' | 'MODERATOR' | 'SUPPORT')[];
}

/**
 * ProtectedRoute component
 * Checks if user is authenticated before rendering children
 * Redirects to login if not authenticated
 */
export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children, requiredRole }) => {
  const location = useLocation();
  const auth = useAppSelector((state) => state.auth);

  // Not authenticated - redirect to login
  if (!auth.isAuthenticated || !auth.user) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  // Check role if required
  if (requiredRole && !requiredRole.includes(auth.user.role)) {
    return <Navigate to="/unauthorized" replace />;
  }

  // Authenticated - render children
  return <>{children}</>;
};

/**
 * PublicRoute component
 * Redirects to dashboard if already authenticated
 */
export const PublicRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const auth = useAppSelector((state) => state.auth);

  if (auth.isAuthenticated) {
    return <Navigate to="/dashboard" replace />;
  }

  return <>{children}</>;
};

export default ProtectedRoute;
