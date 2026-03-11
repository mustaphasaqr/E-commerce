/**
 * ResetPasswordPage
 * User resets their password using the token from email
 */

import React from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { authService } from '../services/authService';
import { ResetPasswordForm } from '../components/ResetPasswordForm';
import styles from './AuthPages.module.css';

/**
 * ResetPasswordPage component
 */
export const ResetPasswordPage: React.FC = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [isLoading, setIsLoading] = React.useState(false);
  const [error, setError] = React.useState<string | null>(null);

  // Get token from URL query parameter (e.g., /reset-password?token=abc123)
  const token = searchParams.get('token');

  // Validate token exists
  React.useEffect(() => {
    if (!token) {
      setError('Invalid or missing reset token. Please request a new password reset.');
    }
  }, [token]);

  const handleResetPassword = async (newPassword: string) => {
    if (!token) return;

    try {
      setIsLoading(true);
      setError(null);

      await authService.completePasswordReset(token, newPassword);

      // Success - navigate to login
      navigate('/login', {
        state: { message: 'Password reset successfully. Please sign in with your new password.' },
      });
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Failed to reset password';
      setError(message);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className={styles.container}>
      <div className={styles.content}>
        {/* Header */}
        <div className={styles.header}>
          <h1 className={styles.title}>Reset Your Password</h1>
          <p className={styles.subtitle}>Enter your new password below</p>
        </div>

        {/* Error if no token */}
        {error && !token && (
          <div className={styles.errorBox}>
            <p>{error}</p>
            <a href="/forgot-password" className={styles.link}>
              Request a new reset link
            </a>
          </div>
        )}

        {/* Reset Password Form */}
        {token && (
          <ResetPasswordForm
            token={token}
            onSuccess={() => navigate('/login')}
            isLoading={isLoading}
            error={error}
            onSubmit={handleResetPassword}
          />
        )}

        {/* Footer */}
        <div className={styles.footer}>
          <span>Don't need to reset? </span>
          <a href="/login" className={styles.link}>
            Sign in here
          </a>
        </div>
      </div>
    </div>
  );
};
