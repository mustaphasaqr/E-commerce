/**
 * LoginForm Component
 * Email/password login form with validation
 */

import React from 'react';
import { useForm } from '@shared/hooks/useForm';
import { validateEmail, validateRequired } from '@shared/utils/validators';
import type { LoginRequest } from '../types/index';
import styles from './LoginForm.module.css';

interface LoginFormProps {
  onLoginSuccess?: () => void;
  isLoading?: boolean;
  error?: string | null;
  onSubmit?: (credentials: LoginRequest) => void | Promise<void>;
}

/**
 * LoginForm component
 */
export const LoginForm: React.FC<LoginFormProps> = ({ onLoginSuccess, isLoading = false, error, onSubmit }) => {
  const validateLoginForm = (values: LoginRequest) => {
    const errors: Partial<Record<keyof LoginRequest, string>> = {};

    if (!validateRequired(values.email)) {
      errors.email = 'Email is required';
    } else if (!validateEmail(values.email)) {
      errors.email = 'Invalid email address';
    }

    if (!validateRequired(values.password)) {
      errors.password = 'Password is required';
    }

    return errors;
  };

  const form = useForm<LoginRequest>({
    initialValues: {
      email: '',
      password: '',
      rememberMe: false,
    },
    validate: validateLoginForm,
    onSubmit: async (values) => {
      await onSubmit?.(values);
      onLoginSuccess?.();
    },
  });

  return (
    <form className={styles.form} onSubmit={form.submitForm}>
      {/* Email field */}
      <div className={styles.formGroup}>
        <input
          id="email"
          type="email"
          {...form.getFieldProps('email')}
          placeholder="Email address"
          disabled={isLoading}
          className={styles.input}
        />
        {form.touched.email && form.errors.email && (
          <span className={styles.errorMessage}>{form.errors.email}</span>
        )}
      </div>

      {/* Password field */}
      <div className={styles.formGroup}>
        <input
          id="password"
          type="password"
          {...form.getFieldProps('password')}
          placeholder="Password"
          disabled={isLoading}
          className={styles.input}
        />
        {form.touched.password && form.errors.password && (
          <span className={styles.errorMessage}>{form.errors.password}</span>
        )}
      </div>

      {/* Remember me */}
      <div className={styles.formGroup}>
        <label className={styles.checkboxLabel}>
          <input
            type="checkbox"
            {...form.getFieldProps('rememberMe')}
            disabled={isLoading}
            className={styles.checkbox}
          />
          <span>Remember me</span>
        </label>
      </div>

      {/* Error message */}
      {error && (
        <div className={styles.alert}>
          <span>{error}</span>
        </div>
      )}

      {/* Submit button */}
      <button
        type="submit"
        disabled={isLoading || !form.isValid}
        className={styles.button}
      >
        {isLoading ? 'Signing in...' : 'Sign In'}
      </button>

      {/* Forgot password link */}
      <div className={styles.footer}>
        <a href="/forgot-password" className={styles.link}>
          Forgot password?
        </a>
      </div>
    </form>
  );
};
