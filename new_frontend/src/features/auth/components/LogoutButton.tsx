import { LogOut, AlertCircle } from 'lucide-react'
import { useLogout } from '../hooks/useLogout'
import { Button } from '@/shared/components/ui'

interface LogoutButtonProps {
  onLogoutSuccess?: () => void
  className?: string
  variant?: 'default' | 'destructive' | 'outline' | 'secondary' | 'ghost'
}

/**
 * LogoutButton Component (shadcn/ui + Lucide)
 *
 * Design Stack:
 * - shadcn/ui Button component
 * - Lucide Icons (LogOut, AlertCircle)
 * - Tailwind CSS styling
 *
 * Features:
 * - Simple logout button with icon
 * - Clears auth store and tokens
 * - Shows loading state while submitting
 * - Shows error message if logout fails
 * - Customizable button variant
 *
 * Formula: Same as test uses
 * - Calls useLogout hook (which calls service)
 * - Service returns void (204 No Content)
 * - Store is cleared on success
 */
export function LogoutButton({ onLogoutSuccess, className = '', variant = 'destructive' }: LogoutButtonProps) {
  const { logout, loading, error } = useLogout()

  const handleLogout = async () => {
    await logout()
    onLogoutSuccess?.()
  }

  return (
    <div className="space-y-2">
      <Button
        onClick={handleLogout}
        disabled={loading}
        variant={variant}
        className={`flex items-center justify-center gap-2 ${className}`}
        aria-label="Logout from your account"
      >
        {loading ? (
          <>
            <span className="inline-block animate-spin">⏳</span>
            <span>Logging out...</span>
          </>
        ) : (
          <>
            <LogOut size={18} />
            <span>Logout</span>
          </>
        )}
      </Button>

      {/* Error Message */}
      {error && (
        <div className="p-3 bg-red-50 border border-red-200 rounded-lg flex items-start gap-2">
          <AlertCircle size={18} className="text-red-600 flex-shrink-0 mt-0.5" />
          <div className="flex-1">
            <p className="text-sm text-red-800 font-medium">Logout Failed</p>
            <p className="text-sm text-red-700 mt-1">{error}</p>
          </div>
          <button
            onClick={handleLogout}
            className="text-red-600 hover:text-red-800 text-sm font-semibold transition"
            aria-label="Retry logout"
          >
            Retry
          </button>
        </div>
      )}
    </div>
  )
}
