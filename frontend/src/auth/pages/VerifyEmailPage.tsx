/**
 * VerifyEmailPage Component
 * Email verification page with token handling
 */

import React from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { VerifyEmailForm } from '../components/VerifyEmailForm';
import styles from './AuthPages.module.css';

/**
 * VerifyEmailPage component
 * URL pattern: /email-verification?token=abc123def456
 */
export const VerifyEmailPage: React.FC = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const [isLoading, setIsLoading] = React.useState(false);
  const [error, setError] = React.useState<string | null>(null);

  const token = searchParams.get('token');

  // Validate token on mount
  React.useEffect(() => {
    if (!token) {
      setError('Invalid verification link. Please request a new verification email.');
    }
  }, [token]);

  const handleVerifyEmail = async () => {
    if (!token) {
      setError('Invalid verification link');
      return;
    }

    try {
      setIsLoading(true);
      setError(null);

      // Call verify email API
      // TODO: awaiting verifyEmail endpoint implementation
      // await authService.verifyEmail(token);

      // Show success and redirect to login
      navigate('/login', {
        state: { message: 'Email verified successfully! You can now log in.' },
      });
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Failed to verify email. Please try again.';
      setError(message);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className={styles.authContainer}>
      <div className={styles.authCard}>
        <h1>Verify Email</h1>

        {/* Token validation error */}
        {error && !token && (
          <div className={styles.errorBox}>
            <p>{error}</p>
            <button onClick={() => navigate('/forgot-password')} className={styles.secondaryLink}>
              Request new verification email
            </button>
          </div>
        )}

        {/* Verify Email Form */}
        {token && (
          <VerifyEmailForm
            onSuccess={() => navigate('/login')}
            isLoading={isLoading}
            error={error}
            onSubmit={handleVerifyEmail}
          />
        )}

        {/* Footer links */}
        <div className={styles.footer}>
          <p>
            Already have verified email?{' '}
            <button onClick={() => navigate('/login')} className={styles.secondaryLink}>
              Back to login
            </button>
          </p>
        </div>
      </div>
    </div>
  );
};
