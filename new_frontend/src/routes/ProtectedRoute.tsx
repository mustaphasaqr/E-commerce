import { Navigate } from 'react-router-dom'
import { useAuthStore } from '@/features/auth/store/authSlice'

type RoleType = 'CUSTOMER' | 'ADMIN' | 'OWNER';
interface ProtectedRouteProps {
  children: React.ReactNode
  requiredRole?: RoleType | RoleType[];
}

/**
 * ProtectedRoute - Guards routes that require authentication
 *
 * Usage:
 * <ProtectedRoute>
 *   <UserProfilePage />
 * </ProtectedRoute>
 *
 * Or with role requirement:
 * <ProtectedRoute requiredRole="admin">
 *   <AdminDashboard />
 * </ProtectedRoute>
 *
 * Behavior:
 * - If user not authenticated → redirect to /login
 * - If user authenticated but wrong role → redirect to /
 * - If user authenticated with correct role → show children
 *
 * Monitored: Redirects logged to console with details
 */
export function ProtectedRoute({ children, requiredRole }: ProtectedRouteProps) {
  const { isAuthenticated, user } = useAuthStore()

  // Check if user is authenticated
  if (!isAuthenticated) {
    console.warn('🔐 Route protected: User not authenticated, redirecting to /login')
    return <Navigate to="/login" replace />
  }


  // Check if user has required role (single or array)
  if (requiredRole) {
    const allowedRoles = Array.isArray(requiredRole) ? requiredRole : [requiredRole];
    if (!user || !allowedRoles.includes(user.role as RoleType)) {
      console.warn(
        `🔐 Route protected: User role "${user?.role}" does not match required roles [${allowedRoles.join(', ')}], redirecting to /`
      );
      return <Navigate to="/" replace />;
    }
  }

  console.log(`🔓 Route protected: Access granted to user "${user?.email}"`)

  return <>{children}</>
}
