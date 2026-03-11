/**
 * VerifyEmailForm Component
 * User verifies their email with token
 */

import React from 'react';
import { Button } from '@shared/components/Button';
import styles from './AuthForms.module.css';

interface VerifyEmailFormProps {
  email?: string;
  onSuccess?: () => void;
  isLoading?: boolean;
  error?: string | null;
  onSubmit?: () => void | Promise<void>;
}

/**
 * VerifyEmailForm component
 */
export const VerifyEmailForm: React.FC<VerifyEmailFormProps> = ({
  email,
  onSuccess: _onSuccess,
  isLoading = false,
  error,
  onSubmit,
}) => {
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    await onSubmit?.();
  };

  return (
    <form className={styles.form} onSubmit={handleSubmit}>
      {/* Email display */}
      {email && (
        <div className={styles.formGroup}>
          <p className={styles.helpText}>
            Verifying email: <strong>{email}</strong>
          </p>
        </div>
      )}

      {/* Info message */}
      <div className={styles.formGroup}>
        <p className={styles.helpText}>
          Click the button below to verify your email address. You should receive a verification link
          in your inbox if you haven't already.
        </p>
      </div>

      {/* Error message */}
      {error && (
        <div className={styles.alert}>
          <span>{error}</span>
        </div>
      )}

      {/* Submit button */}
      <Button
        type="submit"
        variant="primary"
        size="lg"
        fullWidth
        disabled={isLoading}
      >
        {isLoading ? 'Verifying...' : 'Verify Email'}
      </Button>
    </form>
  );
};
