/**
 * ForgotPasswordPage
 * User forgot their password and want to reset it
 */

import React from 'react';
import { useNavigate } from 'react-router-dom';
import { authService } from '../services/authService';
import { ForgotPasswordForm } from '../components/ForgotPasswordForm';
import styles from './AuthPages.module.css';

/**
 * ForgotPasswordPage component
 */
export const ForgotPasswordPage: React.FC = () => {
  const navigate = useNavigate();
  const [isLoading, setIsLoading] = React.useState(false);
  const [error, setError] = React.useState<string | null>(null);
  const [successMessage, setSuccessMessage] = React.useState<string | null>(null);

  const handleForgotPassword = async (email: string) => {
    try {
      setIsLoading(true);
      setError(null);
      setSuccessMessage(null);

      await authService.requestPasswordReset(email);

      setSuccessMessage('Password reset link has been sent to your email. Please check your inbox.');

      // Wait a bit and then navigate to login
      setTimeout(() => {
        navigate('/login');
      }, 3000);
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Failed to request password reset';
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
          <h1 className={styles.title}>Forgot Your Password?</h1>
          <p className={styles.subtitle}>
            Enter your email address and we'll send you a link to reset your password
          </p>
        </div>

        {/* Forgot Password Form */}
        <ForgotPasswordForm
          onSuccess={() => navigate('/login')}
          isLoading={isLoading}
          error={error}
          successMessage={successMessage}
          onSubmit={handleForgotPassword}
        />

        {/* Footer */}
        <div className={styles.footer}>
          <span>Remember your password? </span>
          <a href="/login" className={styles.link}>
            Sign in now
          </a>
        </div>
      </div>
    </div>
  );
};
