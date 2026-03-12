import { useLogout } from '../hooks/useLogout'

interface LogoutButtonProps {
  onLogoutSuccess?: () => void
  className?: string
  variant?: 'primary' | 'secondary' | 'ghost'
}

/**
 * LogoutButton Component
 *
 * Features:
 * - Simple logout button
 * - Clears auth store and tokens
 * - Shows loading spinner while submitting
 * - Shows error message if logout fails
 * - Customizable styling/variant
 *
 * Formula: Same as test uses
 * - Calls useLogout hook (which calls service)
 * - Service returns void (204 No Content)
 * - Store is cleared on success
 */
export function LogoutButton({ onLogoutSuccess, className = '', variant = 'primary' }: LogoutButtonProps) {
  const { logout, loading, error } = useLogout()

  const handleLogout = async () => {
    await logout()
    onLogoutSuccess?.()
  }

  const variantStyles = {
    primary: 'bg-red-600 hover:bg-red-700 active:bg-red-800 text-white',
    secondary: 'bg-gray-200 hover:bg-gray-300 active:bg-gray-400 text-gray-900',
    ghost: 'text-gray-700 hover:text-red-600 hover:bg-gray-100',
  }

  return (
    <div>
      <button
        onClick={handleLogout}
        disabled={loading}
        className={`px-4 py-2 rounded-lg font-semibold transition flex items-center justify-center gap-2 ${
          loading ? 'opacity-50 cursor-not-allowed' : ''
        } ${variantStyles[variant]} ${className}`}
      >
        {loading ? (
          <>
            <span className="inline-block w-4 h-4 border-2 border-current border-t-transparent rounded-full animate-spin"></span>
            Logging out...
          </>
        ) : (
          '🚪 Logout'
        )}
      </button>

      {/* Error Message */}
      {error && (
        <div className="mt-2 p-2 bg-red-50 border border-red-200 rounded text-red-800 text-sm">
          <span className="font-semibold">❌ Error:</span> {error}
          <button
            onClick={() => {}}
            className="ml-2 text-red-600 hover:text-red-800 underline text-xs"
          >
            Retry
          </button>
        </div>
      )}
    </div>
  )
}
