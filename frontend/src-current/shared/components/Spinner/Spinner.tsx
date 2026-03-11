/**
 * Spinner Component
 * Loading indicator
 */

import React from 'react';
import styles from './Spinner.module.css';

/**
 * Spinner sizes
 */
export type SpinnerSize = 'sm' | 'md' | 'lg';

/**
 * Spinner props
 */
export interface SpinnerProps {
  size?: SpinnerSize;
  label?: string;
  fullScreen?: boolean;
  className?: string;
}

/**
 * Spinner component
 */
export const Spinner: React.FC<SpinnerProps> = ({
  size = 'md',
  label,
  fullScreen = false,
  className,
}) => {
  const spinnerClasses = [
    styles.spinner,
    styles[`size-${size}`],
    fullScreen && styles.fullScreen,
    className,
  ]
    .filter(Boolean)
    .join(' ');

  return (
    <div className={spinnerClasses} role="status" aria-live="polite">
      <div className={styles.ring}></div>
      <div className={styles.ring}></div>
      <div className={styles.ring}></div>
      {label && <p className={styles.label}>{label}</p>}
    </div>
  );
};

/**
 * Skeleton loader (placeholder while loading)
 */
export interface SkeletonProps {
  width?: string | number;
  height?: string | number;
  borderRadius?: string | number;
  className?: string;
}

/**
 * Skeleton component
 */
export const Skeleton: React.FC<SkeletonProps> = ({
  width = '100%',
  height = '20px',
  borderRadius = '4px',
  className,
}) => {
  const style: React.CSSProperties = {
    width,
    height,
    borderRadius: typeof borderRadius === 'number' ? `${borderRadius}px` : borderRadius,
  };

  return (
    <div
      className={[styles.skeleton, className].filter(Boolean).join(' ')}
      style={style}
      aria-busy="true"
      role="status"
      aria-label="Loading..."
    />
  );
};

/**
 * Skeleton group for loading multiple elements
 */
export interface SkeletonGroupProps {
  count: number;
  height?: string | number;
  gap?: string | number;
  className?: string;
}

/**
 * SkeletonGroup component
 */
export const SkeletonGroup: React.FC<SkeletonGroupProps> = ({
  count,
  height = '20px',
  gap = '1rem',
  className,
}) => {
  const groupStyle: React.CSSProperties = {
    display: 'flex',
    flexDirection: 'column',
    gap: typeof gap === 'number' ? `${gap}px` : gap,
  };

  return (
    <div style={groupStyle} className={className}>
      {Array.from({ length: count }).map((_, i) => (
        <Skeleton key={i} height={height} />
      ))}
    </div>
  );
};
