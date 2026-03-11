/**
 * Card Component
 * Container component for content grouping
 */

import React, { ReactNode } from 'react';
import styles from './Card.module.css';

/**
 * Card props
 */
export interface CardProps {
  children: ReactNode;
  className?: string;
  onClick?: () => void;
  hoverable?: boolean;
  padding?: 'sm' | 'md' | 'lg' | 'none';
  shadow?: 'sm' | 'md' | 'lg' | 'none';
}

/**
 * Card component
 */
export const Card = React.forwardRef<HTMLDivElement, CardProps>(
  (
    {
      children,
      className,
      onClick,
      hoverable = false,
      padding = 'md',
      shadow = 'md',
    },
    ref
  ) => {
    const cardClasses = [
      styles.card,
      styles[`padding-${padding}`],
      styles[`shadow-${shadow}`],
      hoverable && styles.hoverable,
      onClick && styles.clickable,
      className,
    ]
      .filter(Boolean)
      .join(' ');

    return (
      <div
        ref={ref}
        className={cardClasses}
        onClick={onClick}
        role={onClick ? 'button' : 'article'}
        tabIndex={onClick ? 0 : -1}
        onKeyDown={
          onClick
            ? (e) => {
                if (e.key === 'Enter' || e.key === ' ') {
                  onClick();
                }
              }
            : undefined
        }
      >
        {children}
      </div>
    );
  }
);

Card.displayName = 'Card';
