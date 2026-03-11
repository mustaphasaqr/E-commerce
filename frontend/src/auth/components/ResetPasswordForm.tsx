/**
 * ResetPasswordForm Component
 * User enters new password with reset token
 */

import React from 'react';
import { useForm } from '@shared/hooks/useForm';
import { validateRequired, validatePassword, validateMinLength } from '@shared/utils/validators';
import { Button } from '@shared/components/Button';
import { Input } from '@shared/components/Input';
import styles from './AuthForms.module.css';

interface ResetPasswordFormProps {
  token: string;
  onSuccess?: () => void;
  isLoading?: boolean;
  error?: string | null;
  onSubmit?: (newPassword: string) => void | Promise<void>;
}

interface ResetPasswordFormData {
  token: string;
  newPassword: string;
  confirmPassword: string;
}

/**
 * ResetPasswordForm component
 */
export const ResetPasswordForm: React.FC<ResetPasswordFormProps> = ({
  token,
  onSuccess: _onSuccess,
  isLoading = false,
  error,
  onSubmit,
}) => {
  const [passwordStrength, setPasswordStrength] = React.useState<'weak' | 'medium' | 'strong'>('weak');

  const validateForm = (values: ResetPasswordFormData) => {
    const errors: Partial<Record<keyof ResetPasswordFormData, string>> = {};

    // New password
    if (!validateRequired(values.newPassword)) {
      errors.newPassword = 'New password is required';
    } else if (!validateMinLength(values.newPassword, 8)) {
      errors.newPassword = 'Password must be at least 8 characters';
    } else {
      const validation = validatePassword(values.newPassword);
      if (!validation.isValid) {
        errors.newPassword = 'Password must include uppercase, lowercase, number, and special character';
      }
    }

    // Confirm password
    if (!validateRequired(values.confirmPassword)) {
      errors.confirmPassword = 'Please confirm your password';
    } else if (values.newPassword !== values.confirmPassword) {
      errors.confirmPassword = 'Passwords do not match';
    }

    return errors;
  };

  const form = useForm<ResetPasswordFormData>({
    initialValues: {
      token,
      newPassword: '',
      confirmPassword: '',
    },
    validate: validateForm,
    onSubmit: async (values) => {
      await onSubmit?.(values.newPassword);
    },
  });

  // Update password strength
  React.useEffect(() => {
    if (form.values.newPassword) {
      const validation = validatePassword(form.values.newPassword);
      setPasswordStrength(validation.strength as 'weak' | 'medium' | 'strong');
    }
  }, [form.values.newPassword]);

  return (
    <form className={styles.form} onSubmit={form.submitForm} aria-label="Reset password form">
      {/* New Password */}
      <div className={styles.formGroup}>
        <label htmlFor="newPassword" className={styles.label}>
          New Password <span className={styles.required}>*</span>
        </label>
        <Input
          id="newPassword"
          type="password"
          {...form.getFieldProps('newPassword')}
          placeholder="Create a strong password"
          disabled={isLoading}
          error={form.touched.newPassword ? form.errors.newPassword : undefined}
          required
        />

        {/* Password strength indicator */}
        {form.values.newPassword && (
          <div className={styles.strengthContainer} role="status" aria-live="polite" aria-atomic="true">
            <div className={`${styles.strengthBar} ${styles[`strength-${passwordStrength}`]}`} />
            <span className={`${styles.strengthLabel} ${styles[`label-${passwordStrength}`]}`}>
              {passwordStrength.charAt(0).toUpperCase() + passwordStrength.slice(1)} password strength
            </span>
          </div>
        )}
      </div>

      {/* Confirm Password */}
      <div className={styles.formGroup}>
        <label htmlFor="confirmPassword" className={styles.label}>
          Confirm Password <span className={styles.required}>*</span>
        </label>
        <Input
          id="confirmPassword"
          type="password"
          {...form.getFieldProps('confirmPassword')}
          placeholder="Confirm your password"
          disabled={isLoading}
          error={form.touched.confirmPassword ? form.errors.confirmPassword : undefined}
          required
        />
      </div>

      {/* Error message */}
      {error && (
        <div className={styles.alert} role="alert" aria-live="assertive">
          {error}
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
        {isLoading ? 'Resetting...' : 'Reset Password'}
      </Button>
    </form>
  );
};
