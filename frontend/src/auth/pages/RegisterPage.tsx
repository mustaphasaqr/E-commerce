/**
 * RegisterPage
 * User registration page with modern design matching FlowyCart
 */

import React from 'react';
import { useNavigate } from 'react-router-dom';
import { RegisterForm } from '../components/RegisterForm';
import styles from './Register.module.scss';
import type { RegisterRequest } from '../types/index';

/**
 * RegisterPage component
 */
export const RegisterPage: React.FC = () => {
  const navigate = useNavigate();

  // Handle registration submission
  const handleRegister = async (_data: RegisterRequest) => {
    try {
      // TODO: Call register from useAuth hook
      // Navigate on success
      navigate('/login');
    } catch (err) {
      // Error is displayed via form
    }
  };

  return (
    <section className={styles.section}>
      <div className={styles.container}>
        <div className={styles.registerContainer}>
          {/* Header */}
          <div className={styles.header}>
            <h1 className={styles.title}>Create Account</h1>
            <p className={styles.subtitle}>Join us and start shopping today</p>
          </div>

          {/* Register Form */}
          <RegisterForm
            onRegisterSuccess={() => navigate('/login')}
            onSubmit={handleRegister}
          />

          {/* Sign In Link */}
          <div className={styles.signinLink}>
            Already have an account? <a href="/login">Sign in here</a>
          </div>
        </div>

        {/* Benefits Box */}
        <div className={styles.benefitsBox}>
          <h3 className={styles.benefitsTitle}>What you'll get</h3>
          <ul className={styles.benefitsList}>
            <li>✓ Express checkout with saved addresses</li>
            <li>✓ Order history and tracking</li>
            <li>✓ Personalized recommendations</li>
            <li>✓ Exclusive member deals</li>
          </ul>
        </div>
      </div>
    </section>
  );
};

export default RegisterPage;
