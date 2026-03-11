/**
 * RootLayout Component
 * Main application layout with header, sidebar, footer
 */

import React from 'react';
import { Outlet } from 'react-router-dom';
import styles from './RootLayout.module.css';

/**
 * RootLayout component
 */
const RootLayout: React.FC = () => {
  return (
    <div className={styles.layout}>
      {/* Header */}
      <header className={styles.header}>
        <div className={styles.headerContent}>
          <h1 className={styles.logo}>E-Commerce</h1>
          <nav className={styles.nav}>
            <a href="/products">Shop</a>
            <a href="/cart">Cart</a>
            <a href="/orders">Orders</a>
            <a href="/profile">Profile</a>
          </nav>
        </div>
      </header>

      {/* Main Content */}
      <main className={styles.main}>
        <Outlet />
      </main>

      {/* Footer */}
      <footer className={styles.footer}>
        <p>&copy; 2026 E-Commerce. All rights reserved.</p>
      </footer>
    </div>
  );
};

export default RootLayout;
