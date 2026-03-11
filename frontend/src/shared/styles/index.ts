/**
 * Global Styles Export
 * Import global styles and provide utilities for theme/CSS variables
 */

import './globals.css';

/**
 * CSS Variable utilities
 */
export const CSSVariables = {
  // Colors
  colors: {
    primary: 'var(--color-primary-600)',
    secondary: 'var(--color-secondary-600)',
    success: 'var(--color-success-600)',
    warning: 'var(--color-warning-600)',
    danger: 'var(--color-danger-600)',
    info: 'var(--color-info-600)',
    text: 'var(--color-text-primary)',
    textSecondary: 'var(--color-text-secondary)',
    bg: 'var(--color-bg-primary)',
    border: 'var(--color-border-default)',
  },

  // Spacing
  spacing: {
    xs: 'var(--spacing-xs)',
    sm: 'var(--spacing-sm)',
    md: 'var(--spacing-md)',
    lg: 'var(--spacing-lg)',
    xl: 'var(--spacing-xl)',
  },

  // Typography
  typography: {
    fontSize: {
      xs: 'var(--font-size-xs)',
      sm: 'var(--font-size-sm)',
      base: 'var(--font-size-base)',
      lg: 'var(--font-size-lg)',
      xl: 'var(--font-size-xl)',
    },
    fontWeight: {
      light: 'var(--font-weight-light)',
      normal: 'var(--font-weight-normal)',
      medium: 'var(--font-weight-medium)',
      bold: 'var(--font-weight-bold)',
    },
    lineHeight: {
      tight: 'var(--line-height-tight)',
      normal: 'var(--line-height-normal)',
      relaxed: 'var(--line-height-relaxed)',
    },
  },

  // Shadows
  shadows: {
    sm: 'var(--shadow-sm)',
    md: 'var(--shadow-md)',
    lg: 'var(--shadow-lg)',
    xl: 'var(--shadow-xl)',
  },

  // Border radius
  radius: {
    sm: 'var(--radius-sm)',
    md: 'var(--radius-md)',
    lg: 'var(--radius-lg)',
    full: 'var(--radius-full)',
  },

  // Transitions
  transitions: {
    fast: 'var(--transition-fast)',
    base: 'var(--transition-base)',
    slow: 'var(--transition-slow)',
  },

  // Z-index
  zIndex: {
    dropdown: 'var(--z-dropdown)',
    modal: 'var(--z-modal)',
    tooltip: 'var(--z-tooltip)',
  },
};

/**
 * Get CSS variable value from computed styles
 */
export const getCSSVariable = (variableName: string): string => {
  if (typeof window === 'undefined') return '';
  return getComputedStyle(document.documentElement)
    .getPropertyValue(`--${variableName}`)
    .trim();
};

/**
 * Set CSS variable value
 */
export const setCSSVariable = (variableName: string, value: string): void => {
  if (typeof document === 'undefined') return;
  document.documentElement.style.setProperty(`--${variableName}`, value);
};

export default CSSVariables;
