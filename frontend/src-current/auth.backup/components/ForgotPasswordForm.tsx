/**
 * ForgotPasswordForm Component
 * User enters email to request password reset
 */

import React from 'react';
import { useForm } from '@shared/hooks/useForm';
import { validateEmail, validateRequired } from '@shared/utils/validators';
import { Button } from '@shared/components/Button';
import { Input } from '@shared/components/Input';
import styles from './AuthForms.module.css';

interface ForgotPasswordFormProps {
  onSuccess?: () => void;
  isLoading?: boolean;
  error?: string | null;
  successMessage?: string | null;
  onSubmit?: (email: string) => void | Promise<void>;
}

interface ForgotPasswordFormData {
  email: string;
}

/**
 * ForgotPasswordForm component
 */
export const ForgotPasswordForm: React.FC<ForgotPasswordFormProps> = ({
  onSuccess: _onSuccess,
  isLoading = false,
  error,
  successMessage,
  onSubmit,
}) => {
  const validateForm = (values: ForgotPasswordFormData) => {
    const errors: Partial<Record<keyof ForgotPasswordFormData, string>> = {};

    if (!validateRequired(values.email)) {
      errors.email = 'Email is required';
    } else if (!validateEmail(values.email)) {
      errors.email = 'Invalid email address';
    }

    return errors;
  };

  const form = useForm<ForgotPasswordFormData>({
    initialValues: {
      email: '',
    },
    validate: validateForm,
    onSubmit: async (values) => {
      await onSubmit?.(values.email);
    },
  });

  return (
    <form className={styles.form} onSubmit={form.submitForm}>
      {/* Email */}
      <div className={styles.formGroup}>
        <label htmlFor="email" className={styles.label}>
          Email Address
        </label>
        <Input
          id="email"
          type="email"
          {...form.getFieldProps('email')}
          placeholder="Enter your email"
          disabled={isLoading}
          error={form.touched.email ? form.errors.email : undefined}
        />
        {form.touched.email && form.errors.email && (
          <span className={styles.errorMessage}>{form.errors.email}</span>
        )}
      </div>

      {/* Success message */}
      {successMessage && (
        <div className={styles.successAlert}>
          <span>{successMessage}</span>
        </div>
      )}

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
        disabled={isLoading || !form.isValid}
        isLoading={isLoading}
      >
        {isLoading ? 'Sending...' : 'Send Reset Link'}
      </Button>

      {/* Info message */}
      <p className={styles.helpText}>
        We'll send a password reset link to your email address.
      </p>
    </form>
  );
};
