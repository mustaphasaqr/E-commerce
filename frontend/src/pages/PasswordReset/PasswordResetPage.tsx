import React, { useState } from 'react';
import { authService } from '@shared/services';
import styles from './index.module.scss';

/**
 * Password Reset Page
 * Integrates with:
 * - POST /api/v1/auth/password-reset/request
 * - POST /api/v1/auth/password-reset/complete
 */
export const PasswordResetPage: React.FC = () => {
  const [step, setStep] = useState<'request' | 'reset'>('request');
  const [email, setEmail] = useState('');
  const [resetToken, setResetToken] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  // const [success, setSuccess] = useState(false);
  const [message, setMessage] = useState<string | null>(null);

  const handleRequestReset = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!email) {
      setError('Please enter your email');
      return;
    }

    try {
      setLoading(true);
      setError(null);
      
      await authService.requestPasswordReset(email);
      
      setMessage('Password reset link sent to your email. Please check your inbox.');
      setTimeout(() => {
        setMessage(null);
        setStep('reset');
      }, 3000);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to send reset link. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleCompleteReset = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!resetToken || !newPassword || !confirmPassword) {
      setError('Please fill in all fields');
      return;
    }

    if (newPassword !== confirmPassword) {
      setError('Passwords do not match');
      return;
    }

    if (newPassword.length < 8) {
      setError('Password must be at least 8 characters');
      return;
    }

    try {
      setLoading(true);
      setError(null);
      
      await authService.completePasswordReset(resetToken, newPassword);
      
      setMessage('Password has been reset successfully! Redirecting to login...');
      
      setTimeout(() => {
        window.location.href = '/login';
      }, 2000);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to reset password. Token may have expired.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <section className={styles.section}>
      <div className={styles.resetContainer}>
        <div className={styles.resetCard}>
          <h1>Reset Your Password</h1>
          
          {error && <div className={`${styles.alert} ${styles.alertError}`}>{error}</div>}
          {message && <div className={`${styles.alert} ${styles.alertSuccess}`}>{message}</div>}

          {step === 'request' ? (
            <form onSubmit={handleRequestReset} className={styles.resetForm}>
              <p className={styles.description}>
                Enter your email address and we'll send you a link to reset your password.
              </p>

              <div className={styles.formGroup}>
                <label htmlFor="email">Email Address</label>
                <input
                  id="email"
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="Enter your email"
                  disabled={loading}
                  required
                />
              </div>

              <button 
                type="submit"
                disabled={loading}
              >
                {loading ? 'Sending...' : 'Send Reset Link'}
              </button>

              <p className={styles.formNote}>
                Remember your password? <a href="/login">Back to login</a>
              </p>
            </form>
          ) : (
            <form onSubmit={handleCompleteReset} className={styles.resetForm}>
              <p className={styles.description}>
                Enter the reset token from your email and your new password.
              </p>

              <div className={styles.formGroup}>
                <label htmlFor="resetToken">Reset Token</label>
                <input
                  id="resetToken"
                  type="text"
                  value={resetToken}
                  onChange={(e) => setResetToken(e.target.value)}
                  placeholder="Paste the token from your email"
                  disabled={loading}
                  required
                />
              </div>

              <div className={styles.formGroup}>
                <label htmlFor="newPassword">New Password</label>
                <input
                  id="newPassword"
                  type="password"
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                  placeholder="Enter new password"
                  disabled={loading}
                  required
                />
              </div>

              <div className={styles.formGroup}>
                <label htmlFor="confirmPassword">Confirm Password</label>
                <input
                  id="confirmPassword"
                  type="password"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  placeholder="Confirm new password"
                  disabled={loading}
                  required
                />
              </div>

              <button 
                type="submit"
                disabled={loading}
              >
                {loading ? 'Resetting...' : 'Reset Password'}
              </button>

              <button 
                type="button"
                onClick={() => {
                  setStep('request');
                  setResetToken('');
                  setNewPassword('');
                  setConfirmPassword('');
                  setError(null);
                }}
                disabled={loading}
              >
                Back to Email Entry
              </button>
            </form>
          )}
        </div>

        <div className={styles.securityInfo}>
          <h3>Security Tips</h3>
          <ul>
            <li>Use a strong, unique password</li>
            <li>Include uppercase, lowercase, numbers, and symbols</li>
            <li>Avoid using personal information</li>
            <li>Don't reuse passwords from other sites</li>
          </ul>
        </div>
      </div>
    </section>
  );
};

export default PasswordResetPage;
