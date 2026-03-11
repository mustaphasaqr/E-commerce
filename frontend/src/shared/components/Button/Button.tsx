/**
 * Button Component
 * Reusable button with multiple variants and states
 */

import React, { ReactNode } from 'react';
import styles from './Button.module.css';

/**
 * Button variants
 */
export type ButtonVariant = 'primary' | 'secondary' | 'danger' | 'success' | 'outline';

/**
 * Button sizes
 */
export type ButtonSize = 'sm' | 'md' | 'lg';

/**
 * Button props
 */
export interface ButtonProps
  extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  children: ReactNode;
  variant?: ButtonVariant;
  size?: ButtonSize;
  isLoading?: boolean;
  isDisabled?: boolean;
  fullWidth?: boolean;
  icon?: ReactNode;
  iconPosition?: 'left' | 'right';
  onClick?: (e: React.MouseEvent<HTMLButtonElement>) => void | Promise<void>;
}

/**
 * Button component
 */
export const Button = React.forwardRef<HTMLButtonElement, ButtonProps>(
  (
    {
      children,
      variant = 'primary',
      size = 'md',
      isLoading = false,
      isDisabled = false,
      fullWidth = false,
      icon,
      iconPosition = 'left',
      className,
      onClick,
      ...props
    },
    ref
  ) => {
    const handleClick = async (e: React.MouseEvent<HTMLButtonElement>) => {
      if (isLoading || isDisabled) {
        e.preventDefault();
        return;
      }

      try {
        await onClick?.(e);
      } catch (error) {
        console.error('Button click error:', error);
      }
    };

    const buttonClassName = [
      styles.button,
      styles[`variant-${variant}`],
      styles[`size-${size}`],
      isLoading && styles.loading,
      isDisabled && styles.disabled,
      fullWidth && styles.fullWidth,
      className,
    ]
      .filter(Boolean)
      .join(' ');

    return (
      <button
        ref={ref}
        className={buttonClassName}
        disabled={isDisabled || isLoading}
        onClick={handleClick}
        aria-busy={isLoading}
        {...props}
      >
        {isLoading && (
          <span className={styles.spinner} aria-hidden="true">
            ⏳
          </span>
        )}

        {!isLoading && icon && iconPosition === 'left' && (
          <span className={styles.icon}>{icon}</span>
        )}

        <span className={styles.content}>{children}</span>

        {!isLoading && icon && iconPosition === 'right' && (
          <span className={styles.icon}>{icon}</span>
        )}
      </button>
    );
  }
);

Button.displayName = 'Button';
