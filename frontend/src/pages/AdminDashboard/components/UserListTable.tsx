import React from 'react';
import type { User } from '@auth/types';
import styles from './UserListTable/index.module.scss';

interface UserListTableProps {
  users: User[];
  onBlock: (user: User) => void;
  onUnblock: (user: User) => void;
  onActivate: (user: User) => void;
  onDeactivate: (user: User) => void;
  onDelete: (user: User) => void;
  onChangeRole: (user: User) => void;
}

/**
 * User List Table Component
 * Displays users and provides action buttons for all admin operations
 */
const UserListTable: React.FC<UserListTableProps> = ({
  users,
  onBlock,
  onUnblock,
  onActivate,
  onDeactivate,
  onDelete,
  onChangeRole,
}) => {

  return (
    <div className={styles.userListTable}>
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Username</th>
            <th>Email</th>
            <th>Status</th>
            <th>Role</th>
            <th>Email Verified</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {users.map((user) => (
            <tr key={user.id} className={styles.userRow}>
              <td className={styles.id}>{user.id.substring(0, 8)}...</td>
              <td className={styles.username}>{user.username}</td>
              <td className={styles.email}>{user.email}</td>
              <td className={`${styles.status} ${styles[`status${user.status || 'Default'}`.toLowerCase()]}`}>
                {user.status}
              </td>
              <td className={styles.role}>{user.role || 'USER'}</td>
              <td className={styles.verified}>
                {user.emailVerified ? '✓' : '✗'}
              </td>
              <td className={styles.actions}>
                <div className={styles.actionButtons}>
                  {user.status !== 'SUSPENDED' && (
                    <button 
                      onClick={() => onBlock(user)}
                      className={`${styles.btnAction} ${styles.btnBlock}`}
                      title="Block user"
                    >
                      Block
                    </button>
                  )}
                  
                  {user.status === 'SUSPENDED' && (
                    <button 
                      onClick={() => onUnblock(user)}
                      className={`${styles.btnAction} ${styles.btnUnblock}`}
                      title="Unblock user"
                    >
                      Unblock
                    </button>
                  )}

                  {user.status === 'INACTIVE' && (
                    <button 
                      onClick={() => onActivate(user)}
                      className={`${styles.btnAction} ${styles.btnActivate}`}
                      title="Activate user"
                    >
                      Activate
                    </button>
                  )}

                  {user.status === 'ACTIVE' && (
                    <button 
                      onClick={() => onDeactivate(user)}
                      className={`${styles.btnAction} ${styles.btnDeactivate}`}
                      title="Deactivate user"
                    >
                      Deactivate
                    </button>
                  )}

                  <button 
                    onClick={() => onChangeRole(user)}
                    className={`${styles.btnAction} ${styles.btnRole}`}
                    title="Change user role"
                  >
                    Role
                  </button>

                  <button 
                    onClick={() => onDelete(user)}
                    className={`${styles.btnAction} ${styles.btnDelete}`}
                    title="Delete user"
                  >
                    Delete
                  </button>
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default UserListTable;
