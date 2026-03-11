/**
 * LoginPage
 * User login page with form and navigation
 */

import React from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { LoginForm } from '../components/LoginForm';
import styles from './AuthPages.module.css';
import type { LoginRequest } from '../types/index';

/**
 * LoginPage component
 */
export const LoginPage: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { login, isLoading, error } = useAuth();

  // Handle login submission
  const handleLogin = async (credentials: LoginRequest) => {
    try {
      await login(credentials);
    } catch (err) {
      // Error is displayed via error prop
    }
  };

  // If already logged in, redirect to dashboard
  React.useEffect(() => {
    const token = localStorage.getItem('auth_token');
    if (token) {
      const from = location.state?.from?.pathname || '/dashboard';
      navigate(from);
    }
  }, [navigate, location]);

  return (
    <section className={styles.section}>
      <div className={`${styles.container}`}>
        <div className={styles.loginContainer}>
          <h2>Login</h2>
          <LoginForm
            onLoginSuccess={() => {
              const from = location.state?.from?.pathname || '/dashboard';
              navigate(from);
            }}
            isLoading={isLoading}
            error={error}
            onSubmit={handleLogin}
          />

          {/* Signup Link */}
          <div className={styles.signupLink}>
            Don't have an account? <a href="/register">Sign up here</a>
          </div>
        </div>
      </div>

      {/* Demo info (for development) */}
      <div className={styles.infoBox}>
        <p className={styles.infoTitle}>Demo Credentials</p>
        <p className={styles.infoText}>
          Email: <code>customer@example.com</code>
        </p>
        <p className={styles.infoText}>
          Password: <code>Password123!</code>
        </p>
      </div>
    </section>
  );
};

export default LoginPage;
