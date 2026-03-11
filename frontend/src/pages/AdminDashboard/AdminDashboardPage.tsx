import React, { useEffect, useState } from 'react';
import { adminService } from '@shared/services';
import type { User } from '@auth/types';
import UserListTable from './components/UserListTable';
import UserSearchForm from './components/UserSearchForm';
import UserActionsModal from './components/UserActionsModal';
import styles from './index.module.scss';

/**
 * Admin Dashboard Page
 * 
 * Integrates with all 9 admin endpoints:
 * - GET /api/v1/admin/users (list users)
 * - GET /api/v1/admin/users/search (search users GET)
 * - POST /api/v1/admin/users/search (search users POST)
 * - POST /api/v1/admin/users/{id}/block
 * - POST /api/v1/admin/users/{id}/unblock
 * - POST /api/v1/admin/users/{id}/activate
 * - POST /api/v1/admin/users/{id}/deactivate
 * - DELETE /api/v1/admin/users/{id}
 * - POST /api/v1/admin/users/{id}/role
 */
export const AdminDashboardPage: React.FC = () => {
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [pageSize] = useState(20);
  const [selectedUser, setSelectedUser] = useState<User | null>(null);
  const [actionType, setActionType] = useState<'block' | 'unblock' | 'activate' | 'deactivate' | 'delete' | 'role' | null>(null);

  // Load users on mount and when page changes
  useEffect(() => {
    loadUsers();
  }, [currentPage]);

  const loadUsers = async () => {
    try {
      setLoading(true);
      const response = await adminService.listUsers(currentPage, pageSize);
      setUsers((response.content || []) as User[]);
      setTotalPages(response.totalPages || 0);
      setError(null);
    } catch (err: any) {
      setError('Failed to load users');
      console.error('Load error:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = async (filters: any) => {
    try {
      setLoading(true);
      const response = await adminService.searchUsersPost(filters);
      setUsers((response.content || []) as User[]);
      setTotalPages(response.totalPages || 0);
      setCurrentPage(0);
      setError(null);
    } catch (err: any) {
      setError('Search failed');
    } finally {
      setLoading(false);
    }
  };

  const handleAction = (user: User, action: typeof actionType) => {
    setSelectedUser(user);
    setActionType(action);
  };

  const handleActionConfirm = async (reason?: string, newRole?: string) => {
    if (!selectedUser || !actionType) return;

    try {
      setLoading(true);
      
      let updated: any;
      switch (actionType) {
        case 'block':
          updated = await adminService.blockUser(selectedUser.id, { reason: reason || '' });
          break;
        case 'unblock':
          updated = await adminService.unblockUser(selectedUser.id, { reason: reason || '' });
          break;
        case 'activate':
          updated = await adminService.activateUser(selectedUser.id, { activationNote: reason || '' });
          break;
        case 'deactivate':
          updated = await adminService.deactivateUser(selectedUser.id, { reason: reason || '' });
          break;
        case 'delete':
          updated = await adminService.deleteUser(selectedUser.id, { reason: reason || '' });
          break;
        case 'role':
          updated = await adminService.changeUserRole(selectedUser.id, { newRole: newRole || 'USER' });
          break;
      }

      // Update user in list
      setUsers(users.map(u => u.id === selectedUser.id ? (updated as User) : u));
      setSelectedUser(null);
      setActionType(null);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Action failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <section className={styles.section}>
      <div className={`${styles.container} main-container`}>
        <h1>User Management</h1>
        
        {error && <div className={styles.errorBanner}>{error}</div>}

        <div className={styles.adminContent}>
          {/* Search Section */}
          <div className={styles.searchSection}>
            <UserSearchForm onSearch={handleSearch} loading={loading} />
          </div>

          {/* Users Table Section */}
          <div className={styles.usersSection}>
            <div className={styles.sectionHeader}>
              <h2>Users</h2>
              <span className={styles.userCount}>Total: {users.length}</span>
            </div>

            {loading && <div className={styles.loading}>Loading users...</div>}
            {!loading && users.length === 0 && <div className={styles.noData}>No users found</div>}
            
            {!loading && users.length > 0 && (
              <>
                <UserListTable 
                  users={users}
                  onBlock={(user) => handleAction(user, 'block')}
                  onUnblock={(user) => handleAction(user, 'unblock')}
                  onActivate={(user) => handleAction(user, 'activate')}
                  onDeactivate={(user) => handleAction(user, 'deactivate')}
                  onDelete={(user) => handleAction(user, 'delete')}
                  onChangeRole={(user) => handleAction(user, 'role')}
                />

                {/* Pagination */}
                <div className={styles.pagination}>
                  <button 
                    onClick={() => setCurrentPage(Math.max(0, currentPage - 1))}
                    disabled={currentPage === 0}
                  >
                    Previous
                  </button>
                  <span className={styles.pageInfo}>
                    Page {currentPage + 1} of {totalPages}
                  </span>
                  <button 
                    onClick={() => setCurrentPage(Math.min(totalPages - 1, currentPage + 1))}
                    disabled={currentPage >= totalPages - 1}
                  >
                    Next
                  </button>
                </div>
              </>
            )}
          </div>
        </div>
      </div>

      {/* Action Modal */}
      {selectedUser && actionType && (
        <UserActionsModal
          user={selectedUser}
          action={actionType}
          onConfirm={handleActionConfirm}
          onCancel={() => {
            setSelectedUser(null);
            setActionType(null);
          }}
        />
      )}
    </section>
  );
};

export default AdminDashboardPage;
