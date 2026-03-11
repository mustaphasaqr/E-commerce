/**
 * Input Component
 * Reusable text input with validation and error display
 */

import React, { ReactNode } from 'react';
import styles from './Input.module.css';

/**
 * Input types
 */
export type InputType =
  | 'text'
  | 'email'
  | 'password'
  | 'number'
  | 'tel'
  | 'url'
  | 'date'
  | 'time'
  | 'search';

/**
 * Input sizes
 */
export type InputSize = 'sm' | 'md' | 'lg';

/**
 * Input props
 */
export interface InputProps
  extends Omit<React.InputHTMLAttributes<HTMLInputElement>, 'size'> {
  label?: string;
  type?: InputType;
  size?: InputSize;
  error?: string;
  hint?: string;
  icon?: ReactNode;
  iconPosition?: 'left' | 'right';
  isLoading?: boolean;
  isClearable?: boolean;
  onClear?: () => void;
  containerClassName?: string;
  required?: boolean;
}

/**
 * Input component
 */
export const Input = React.forwardRef<HTMLInputElement, InputProps>(
  (
    {
      label,
      type = 'text',
      size = 'md',
      error,
      hint,
      icon,
      iconPosition = 'left',
      isLoading = false,
      isClearable = false,
      onClear,
      containerClassName,
      required,
      className,
      value,
      disabled,
      onChange,
      ...props
    },
    ref
  ) => {
    const handleClear = () => {
      onClear?.();
    };

    const containerClasses = [
      styles.container,
      styles[`size-${size}`],
      error && styles.error,
      disabled && styles.disabled,
      containerClassName,
    ]
      .filter(Boolean)
      .join(' ');

    const inputClasses = [
      styles.input,
      icon && styles[`icon-${iconPosition}`],
      isLoading && styles.loading,
      className,
    ]
      .filter(Boolean)
      .join(' ');

    return (
      <div className={containerClasses}>
        {label && (
          <label className={styles.label}>
            {label}
            {required && <span className={styles.required}>*</span>}
          </label>
        )}

        <div className={styles.inputWrapper}>
          {icon && iconPosition === 'left' && (
            <span className={styles.iconLeft}>{icon}</span>
          )}

          <input
            ref={ref}
            type={type}
            className={inputClasses}
            value={value}
            onChange={onChange}
            disabled={disabled || isLoading}
            {...props}
          />

          {isLoading && <span className={styles.spinner}>⏳</span>}

          {isClearable && value && (
            <button
              type="button"
              className={styles.clearButton}
              onClick={handleClear}
              disabled={disabled}
              aria-label="Clear input"
            >
              ✕
            </button>
          )}

          {icon && iconPosition === 'right' && !isClearable && (
            <span className={styles.iconRight}>{icon}</span>
          )}
        </div>

        {error && <div className={styles.errorText}>{error}</div>}
        {hint && !error && <div className={styles.hintText}>{hint}</div>}
      </div>
    );
  }
);

Input.displayName = 'Input';
