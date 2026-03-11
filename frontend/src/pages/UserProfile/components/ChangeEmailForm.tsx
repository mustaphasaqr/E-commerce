import React, { useState } from 'react';
import { userService } from '@shared/services';
import type { User } from '@auth/types';
import styles from './ChangeEmailForm/index.module.scss';

interface ChangeEmailFormProps {
  user: User;
  onEmailChanged: (newEmail: string) => void;
}

/**
 * Change Email Component
 * Integrates with PUT /api/v1/users/me/email
 */
const ChangeEmailForm: React.FC<ChangeEmailFormProps> = ({ user, onEmailChanged }) => {
  const [newEmail, setNewEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!newEmail || !password) {
      setError('Please fill in all fields');
      return;
    }

    try {
      setLoading(true);
      setError(null);
      
      await userService.changeEmail({
        newEmail,
        password
      });
      
      setSuccess(true);
      onEmailChanged(newEmail);
      setNewEmail('');
      setPassword('');
      
      setTimeout(() => setSuccess(false), 3000);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to change email');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={styles.changeEmailForm}>
      <h3>Change Email Address</h3>
      <p className={styles.currentEmail}>Current email: <strong>{user.email}</strong></p>
      
      {error && <div className={styles.errorAlert}>{error}</div>}
      {success && <div className={styles.successAlert}>Email changed successfully!</div>}
      
      <form onSubmit={handleSubmit}>
        <div className={styles.formGroup}>
          <label htmlFor="newEmail">New Email Address</label>
          <input
            id="newEmail"
            type="email"
            value={newEmail}
            onChange={(e) => setNewEmail(e.target.value)}
            placeholder="Enter new email"
            disabled={loading}
            required
          />
        </div>

        <div className={styles.formGroup}>
          <label htmlFor="password">Confirm with Password</label>
          <input
            id="password"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="Enter your password"
            disabled={loading}
            required
          />
        </div>

        <button type="submit" disabled={loading}>
          {loading ? 'Updating...' : 'Change Email'}
        </button>
      </form>
    </div>
  );
};

export default ChangeEmailForm;
