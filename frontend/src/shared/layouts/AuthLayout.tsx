/**
 * AuthLayout Component
 * Centered layout for auth pages (login, register)
 */

import React from 'react';
import styles from './AuthLayout.module.css';

interface AuthLayoutProps {
  children: React.ReactNode;
}

/**
 * AuthLayout component
 */
const AuthLayout: React.FC<AuthLayoutProps> = ({ children }) => {
  return (
    <div className={styles.layout}>
      <div className={styles.container}>
        {children}
      </div>
    </div>
  );
};

export default AuthLayout;
