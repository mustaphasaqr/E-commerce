import React, { useState } from 'react';
import type { User } from '@auth/types';
import styles from './UserActionsModal/index.module.scss';

interface UserActionsModalProps {
  user: User;
  action: 'block' | 'unblock' | 'activate' | 'deactivate' | 'delete' | 'role';
  onConfirm: (reason?: string, newRole?: string) => void;
  onCancel: () => void;
}

/**
 * User Actions Modal Component
 * Handles all admin user actions:
 * - Block/Unblock
 * - Activate/Deactivate
 * - Delete
 * - Change Role
 */
const UserActionsModal: React.FC<UserActionsModalProps> = ({
  user,
  action,
  onConfirm,
  onCancel,
}) => {
  const [reason, setReason] = useState('');
  const [newRole, setNewRole] = useState('USER');
  const [loading, setLoading] = useState(false);

  const getActionTitle = () => {
    switch (action) {
      case 'block':
        return `Block User ${user.username}`;
      case 'unblock':
        return `Unblock User ${user.username}`;
      case 'activate':
        return `Activate User ${user.username}`;
      case 'deactivate':
        return `Deactivate User ${user.username}`;
      case 'delete':
        return `Delete User ${user.username}`;
      case 'role':
        return `Change Role for ${user.username}`;
    }
  };

  const getActionDescription = () => {
    switch (action) {
      case 'block':
        return 'Block this user from accessing their account';
      case 'unblock':
        return 'Unblock this user to restore access';
      case 'activate':
        return 'Activate this user account';
      case 'deactivate':
        return 'Deactivate this user account';
      case 'delete':
        return 'Permanently delete this user and all associated data';
      case 'role':
        return 'Change the role and permissions for this user';
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      await onConfirm(reason, newRole);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={styles.modalOverlay}>
      <div className={styles.modalDialog}>
        <div className={styles.modalHeader}>
          <h2>{getActionTitle()}</h2>
          <button className={styles.modalClose} onClick={onCancel}>×</button>
        </div>

        <form onSubmit={handleSubmit}>
          <div className={styles.modalBody}>
            <p className={styles.actionDescription}>{getActionDescription()}</p>

            <div className={styles.userInfo}>
              <p><strong>Email:</strong> {user.email}</p>
              <p><strong>Current Status:</strong> {user.status}</p>
              {user.role && <p><strong>Current Role:</strong> {user.role}</p>}
            </div>

            {/* Reason field for block, unblock, activate, deactivate, delete */}
            {['block', 'unblock', 'activate', 'deactivate', 'delete'].includes(action) && (
              <div className={styles.formGroup}>
                <label htmlFor="reason">
                  {action === 'delete' ? 'Deletion Reason' : 'Reason'}
                </label>
                <textarea
                  id="reason"
                  value={reason}
                  onChange={(e) => setReason(e.target.value)}
                  placeholder={`Enter ${action} reason...`}
                  rows={4}
                  disabled={loading}
                />
              </div>
            )}

            {/* Role selection for role change */}
            {action === 'role' && (
              <div className={styles.formGroup}>
                <label htmlFor="newRole">New Role</label>
                <select
                  id="newRole"
                  value={newRole}
                  onChange={(e) => setNewRole(e.target.value)}
                  disabled={loading}
                >
                  <option value="USER">User (Standard access)</option>
                  <option value="ADMIN">Admin (Admin panel access)</option>
                  <option value="OWNER">Owner (Full system access)</option>
                </select>
              </div>
            )}

            {/* Warning for dangerous actions */}
            {action === 'delete' && (
              <div className={styles.warningBox}>
                <p>
                  <strong>⚠️ Warning:</strong> This action cannot be undone. 
                  All user data will be permanently deleted.
                </p>
              </div>
            )}
          </div>

          <div className={styles.modalFooter}>
            <button
              type="button"
              className={styles.btnSecondary}
              onClick={onCancel}
              disabled={loading}
            >
              Cancel
            </button>
            <button
              type="submit"
              className={action === 'delete' ? styles.btnDanger : styles.btnPrimary}
              disabled={loading}
            >
              {loading ? 'Processing...' : `Confirm ${action}`}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default UserActionsModal;
