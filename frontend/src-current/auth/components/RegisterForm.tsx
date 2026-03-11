/**
 * RegisterForm Component
 * User registration form with validation
 */

import React from 'react';
import { useForm } from '@shared/hooks/useForm';
import { validateEmail, validateRequired, validatePassword, validateMinLength } from '@shared/utils/validators';
import { Button } from '@shared/components/Button';
import { Input } from '@shared/components/Input';
import type { RegisterRequest } from '../types/index';
import styles from './RegisterForm.module.css';

interface RegisterFormProps {
  onRegisterSuccess?: () => void;
  isLoading?: boolean;
  error?: string | null;
  onSubmit?: (data: RegisterRequest) => void | Promise<void>;
}

/**
 * RegisterForm component
 */
export const RegisterForm: React.FC<RegisterFormProps> = ({ onRegisterSuccess, isLoading = false, error, onSubmit }) => {
  const [passwordStrength, setPasswordStrength] = React.useState<'weak' | 'medium' | 'strong'>('weak');

  const validateRegisterForm = (values: RegisterRequest) => {
    const errors: Partial<Record<keyof RegisterRequest, string>> = {};

    // Username
    if (!validateRequired(values.username)) {
      errors.username = 'Username is required';
    } else if (!validateMinLength(values.username, 3)) {
      errors.username = 'Username must be at least 3 characters';
    }

    // Email
    if (!validateRequired(values.email)) {
      errors.email = 'Email is required';
    } else if (!validateEmail(values.email)) {
      errors.email = 'Invalid email address';
    }

    // Password
    if (!validateRequired(values.password)) {
      errors.password = 'Password is required';
    } else if (!validateMinLength(values.password, 8)) {
      errors.password = 'Password must be at least 8 characters';
    } else {
      const validation = validatePassword(values.password);
      if (!validation.isValid) {
        errors.password = 'Password must include uppercase, lowercase, number, and special character';
      }
    }

    // Terms accepted
    if (!values.termsAccepted) {
      errors.termsAccepted = 'You must accept the terms and conditions';
    }

    return errors;
  };

  const form = useForm<RegisterRequest>({
    initialValues: {
      username: '',
      email: '',
      password: '',
      termsAccepted: false,
    },
    validate: validateRegisterForm,
    onSubmit: async (values) => {
      await onSubmit?.(values);
      onRegisterSuccess?.();
    },
  });

  // Update password strength
  React.useEffect(() => {
    if (form.values.password) {
      const validation = validatePassword(form.values.password);
      setPasswordStrength(validation.strength as 'weak' | 'medium' | 'strong');
    }
  }, [form.values.password]);

  return (
    <form className={styles.form} onSubmit={form.submitForm}>
      {/* Username */}
      <div className={styles.formGroup}>
        <label htmlFor="username" className={styles.label}>
          Username
        </label>
        <Input
          id="username"
          type="text"
          {...form.getFieldProps('username')}
          placeholder="Choose a username"
          disabled={isLoading}
          error={form.touched.username ? form.errors.username : undefined}
        />
        {form.touched.username && form.errors.username && (
          <span className={styles.errorMessage}>{form.errors.username}</span>
        )}
      </div>

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

      {/* Password */}
      <div className={styles.formGroup}>
        <label htmlFor="password" className={styles.label}>
          Password
        </label>
        <Input
          id="password"
          type="password"
          {...form.getFieldProps('password')}
          placeholder="Create a strong password"
          disabled={isLoading}
          error={form.touched.password ? form.errors.password : undefined}
        />
        
        {/* Password strength indicator */}
        {form.values.password && (
          <div className={styles.strengthContainer}>
            <div className={`${styles.strengthBar} ${styles[`strength-${passwordStrength}`]}`} />
            <span className={`${styles.strengthLabel} ${styles[`label-${passwordStrength}`]}`}>
              {passwordStrength.charAt(0).toUpperCase() + passwordStrength.slice(1)} password
            </span>
          </div>
        )}

        {form.touched.password && form.errors.password && (
          <span className={styles.errorMessage}>{form.errors.password}</span>
        )}
      </div>

      {/* Terms and Conditions */}
      <div className={styles.formGroup}>
        <label className={styles.checkboxLabel}>
          <input
            type="checkbox"
            {...form.getFieldProps('termsAccepted')}
            checked={form.values.termsAccepted}
            disabled={isLoading}
          />
          <span>I accept the terms and conditions</span>
        </label>
        {form.touched.termsAccepted && form.errors.termsAccepted && (
          <span className={styles.errorMessage}>{form.errors.termsAccepted}</span>
        )}
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
        disabled={isLoading || !form.isValid}
        isLoading={isLoading}
      >
        {isLoading ? 'Creating account...' : 'Create Account'}
      </Button>
    </form>
  );
};
