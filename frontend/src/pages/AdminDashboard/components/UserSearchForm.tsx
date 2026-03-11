import React, { useState } from 'react';
import styles from './UserSearchForm/index.module.scss';

interface UserSearchFormProps {
  onSearch: (filters: any) => void;
  loading: boolean;
}

/**
 * User Search Form Component
 * Integrates with GET/POST /api/v1/admin/users/search
 */
const UserSearchForm: React.FC<UserSearchFormProps> = ({ onSearch, loading }) => {
  const [email, setEmail] = useState('');
  const [username, setUsername] = useState('');
  const [status, setStatus] = useState('');
  const [role, setRole] = useState('');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSearch({
      email: email || undefined,
      username: username || undefined,
      status: status || undefined,
      role: role || undefined,
      page: 0,
      size: 20,
    });
  };

  const handleReset = () => {
    setEmail('');
    setUsername('');
    setStatus('');
    setRole('');
    onSearch({
      page: 0,
      size: 20,
    });
  };

  return (
    <form className={styles.userSearchForm} onSubmit={handleSubmit}>
      <h3>Search Users</h3>
      
      <div className={styles.formGrid}>
        <div className={styles.formGroup}>
          <label htmlFor="email">Email</label>
          <input
            id="email"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="Search by email..."
            disabled={loading}
          />
        </div>

        <div className={styles.formGroup}>
          <label htmlFor="username">Username</label>
          <input
            id="username"
            type="text"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            placeholder="Search by username..."
            disabled={loading}
          />
        </div>

        <div className={styles.formGroup}>
          <label htmlFor="status">Status</label>
          <select
            id="status"
            value={status}
            onChange={(e) => setStatus(e.target.value)}
            disabled={loading}
          >
            <option value="">All Statuses</option>
            <option value="ACTIVE">Active</option>
            <option value="INACTIVE">Inactive</option>
            <option value="BLOCKED">Blocked</option>
          </select>
        </div>

        <div className={styles.formGroup}>
          <label htmlFor="role">Role</label>
          <select
            id="role"
            value={role}
            onChange={(e) => setRole(e.target.value)}
            disabled={loading}
          >
            <option value="">All Roles</option>
            <option value="USER">User</option>
            <option value="ADMIN">Admin</option>
            <option value="OWNER">Owner</option>
          </select>
        </div>
      </div>

      <div className={styles.formActions}>
        <button 
          type="submit" 
          className={styles.btnPrimary}
          disabled={loading}
        >
          {loading ? 'Searching...' : 'Search'}
        </button>
        <button 
          type="button" 
          className={styles.btnSecondary}
          onClick={handleReset}
          disabled={loading}
        >
          Reset
        </button>
      </div>
    </form>
  );
};

export default UserSearchForm;
