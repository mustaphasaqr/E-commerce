/**
 * RegisterPage
 * User registration page with form and navigation
 */

import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { RegisterForm } from '../components/RegisterForm';
import styles from './AuthPages.module.css';
import type { RegisterRequest } from '../types/index';

/**
 * RegisterPage component
 */
export const RegisterPage: React.FC = () => {
  const navigate = useNavigate();
  const { register, isLoading, error } = useAuth();

  // Handle registration submission
  const handleRegister = async (data: RegisterRequest) => {
    try {
      await register(data);
      // Navigate on success is handled by form's onRegisterSuccess
    } catch (err) {
      // Error is displayed via error prop
    }
  };

  return (
    <div className={styles.container}>
      <div className={styles.content}>
        {/* Header */}
        <div className={styles.header}>
          <h1 className={styles.title}>Create Account</h1>
          <p className={styles.subtitle}>Join us and start shopping today</p>
        </div>

        {/* Register Form */}
        <RegisterForm
          onRegisterSuccess={() => navigate('/dashboard')}
          isLoading={isLoading}
          error={error}
          onSubmit={handleRegister}
        />

        {/* Footer */}
        <div className={styles.footer}>
          <span>Already have an account? </span>
          <a href="/login" className={styles.link}>
            Sign in here
          </a>
        </div>
      </div>

      {/* Info Box */}
      <div className={styles.infoBox}>
        <p className={styles.infoTitle}>What you'll get</p>
        <ul className={styles.infoList}>
          <li>✓ Express checkout with saved addresses</li>
          <li>✓ Order history and tracking</li>
          <li>✓ Personalized recommendations</li>
          <li>✓ Exclusive member deals</li>
        </ul>
      </div>
    </div>
  );
};

export default RegisterPage;
