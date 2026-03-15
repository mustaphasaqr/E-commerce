import React, { useEffect, useState } from 'react';
import type { AxiosError } from 'axios';
import { Search, ShieldAlert, ShieldCheck, Trash2, UserCheck, UserCog, UserX } from 'lucide-react';
import { Button, Card, CardContent, CardDescription, CardHeader, CardTitle, Input } from '@/shared/components/ui';
import { adminService } from '../api/adminService';
import type { PaginatedUsersResponse, UserListResponse } from '../types';

const PAGE_SIZE = 10;
type UserRole = 'CUSTOMER' | 'EMPLOYEE' | 'OWNER';
type UserStatusFilter = 'ALL' | 'ACTIVE' | 'PENDING' | 'INACTIVE' | 'BLOCKED';

const statusBadgeClass = (status: string) => {
  const normalized = status.toUpperCase();
  if (normalized === 'ACTIVE') return 'bg-emerald-50 text-emerald-700 border-emerald-200';
  if (normalized === 'BLOCKED') return 'bg-rose-50 text-rose-700 border-rose-200';
  if (normalized === 'INACTIVE') return 'bg-amber-50 text-amber-700 border-amber-200';
  if (normalized === 'PENDING') return 'bg-blue-50 text-blue-700 border-blue-200';
  return 'bg-gray-50 text-gray-700 border-gray-200';
};

const getApiErrorMessage = (error: unknown, fallback: string) => {
  const axiosError = error as AxiosError<{ message?: string; error?: string | { message?: string } }>;
  const payload = axiosError?.response?.data;
  return payload?.message ||
    (typeof payload?.error === 'string' ? payload.error : payload?.error?.message) ||
    axiosError?.message ||
    fallback;
};

const UsersPage: React.FC = () => {
  const [users, setUsers] = useState<UserListResponse[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [query, setQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState<UserStatusFilter>('ALL');
  const [roleFilter, setRoleFilter] = useState<'ALL' | UserRole>('ALL');
  const [roleDrafts, setRoleDrafts] = useState<Record<string, UserRole>>({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [busyUserId, setBusyUserId] = useState<string | null>(null);

  const loadUsers = async (
    targetPage: number,
    activeQuery: string,
    activeStatus: UserStatusFilter,
    activeRole: 'ALL' | UserRole
  ) => {
    setLoading(true);
    setError(null);

    try {
      const hasFilters = activeStatus !== 'ALL' || activeRole !== 'ALL';
      const hasQuery = !!activeQuery.trim();

      const response = hasQuery || hasFilters
        ? await adminService.searchUsers(
            targetPage,
            PAGE_SIZE,
            activeQuery.trim(),
            activeStatus === 'ALL' ? undefined : activeStatus,
            activeRole === 'ALL' ? undefined : activeRole
          )
        : await adminService.getUsers(targetPage, PAGE_SIZE);

      const payload: PaginatedUsersResponse = response.data;
      setUsers(payload.users ?? []);
      setRoleDrafts(
        (payload.users ?? []).reduce<Record<string, UserRole>>((acc, user) => {
          acc[user.id] = user.role as UserRole;
          return acc;
        }, {})
      );
      setPage(payload.currentPage ?? targetPage);
      setTotalPages(payload.totalPages ?? 0);
      setTotalElements(payload.totalElements ?? 0);
    } catch (e) {
      setError(getApiErrorMessage(e, 'Failed to load users'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadUsers(0, '', 'ALL', 'ALL');
  }, []);

  const runAction = async (userId: string, action: 'block' | 'unblock' | 'activate' | 'deactivate') => {
    setBusyUserId(userId);
    setError(null);

    try {
      if (action === 'block') {
        await adminService.blockUser(userId, 'Blocked by owner from management table');
      }
      if (action === 'unblock') {
        await adminService.unblockUser(userId, 'Unblocked by owner from management table');
      }
      if (action === 'activate') {
        await adminService.activateUser(userId, 'Activated by owner from management table');
      }
      if (action === 'deactivate') {
        await adminService.deactivateUser(userId, 'Deactivated by owner from management table');
      }

      await loadUsers(page, query, statusFilter, roleFilter);
    } catch (e) {
      setError(getApiErrorMessage(e, `Failed to ${action} user`));
    } finally {
      setBusyUserId(null);
    }
  };

  const runRoleChange = async (userId: string) => {
    const targetRole = roleDrafts[userId];
    const current = users.find((u) => u.id === userId);
    if (!targetRole || !current || current.role === targetRole) {
      return;
    }

    setBusyUserId(userId);
    setError(null);
    try {
      await adminService.changeUserRole(userId, targetRole);
      await loadUsers(page, query, statusFilter, roleFilter);
    } catch (e) {
      setError(getApiErrorMessage(e, 'Failed to change user role'));
    } finally {
      setBusyUserId(null);
    }
  };

  const runDelete = async (userId: string) => {
    setBusyUserId(userId);
    setError(null);

    try {
      await adminService.deleteUser(userId, 'Deleted by owner from management table');
      await loadUsers(page, query, statusFilter, roleFilter);
    } catch (e) {
      setError(getApiErrorMessage(e, 'Failed to delete user'));
    } finally {
      setBusyUserId(null);
    }
  };

  return (
    <div className="relative">
      <div className="pointer-events-none absolute inset-x-0 top-0 h-44 bg-gradient-to-b from-slate-100 to-transparent" />

      <div className="relative mx-auto max-w-[90rem] px-4 sm:px-6 lg:px-8 py-8 sm:py-10 space-y-6">
        <div className="rounded-2xl border border-slate-200/80 bg-white/90 p-5 sm:p-6 shadow-sm">
          <h2 className="text-3xl font-extrabold tracking-tight mb-2">Users Command Center</h2>
          <p className="text-gray-600">Real backend-powered user management with search, filters, role change, block/unblock, activate/deactivate, and delete actions.</p>
        </div>

        <Card>
          <CardHeader>
            <CardTitle>User Operations</CardTitle>
            <CardDescription>Coverage includes all available owner admin-user APIs.</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-4 gap-3">
              <div className="md:col-span-2">
                <Input
                  value={query}
                  onChange={(e) => setQuery(e.target.value)}
                  placeholder="Search by email or username"
                  icon={<Search className="h-4 w-4" />}
                />
              </div>

              <select
                value={statusFilter}
                onChange={(e) => setStatusFilter(e.target.value as UserStatusFilter)}
                className="h-11 rounded-md border border-input bg-background px-3 text-sm"
              >
                <option value="ALL">All Statuses</option>
                <option value="ACTIVE">ACTIVE</option>
                <option value="PENDING">PENDING</option>
                <option value="INACTIVE">INACTIVE</option>
                <option value="BLOCKED">BLOCKED</option>
              </select>

              <select
                value={roleFilter}
                onChange={(e) => setRoleFilter(e.target.value as 'ALL' | UserRole)}
                className="h-11 rounded-md border border-input bg-background px-3 text-sm"
              >
                <option value="ALL">All Roles</option>
                <option value="CUSTOMER">CUSTOMER</option>
                <option value="EMPLOYEE">EMPLOYEE</option>
                <option value="OWNER">OWNER</option>
              </select>
            </div>

            <div className="flex items-center gap-2">
              <Button
                variant="outline"
                onClick={() => loadUsers(0, query, statusFilter, roleFilter)}
                disabled={loading}
              >
                Search
              </Button>
              <Button
                variant="ghost"
                onClick={() => {
                  setQuery('');
                  setStatusFilter('ALL');
                  setRoleFilter('ALL');
                  loadUsers(0, '', 'ALL', 'ALL');
                }}
                disabled={loading}
              >
                Reset
              </Button>
            </div>

          <div className="text-sm text-gray-600">Total users: {totalElements}</div>

          {error && <div className="rounded-md border border-red-200 bg-red-50 p-3 text-sm text-red-700">{error}</div>}

          <div className="overflow-x-auto rounded-lg border border-gray-200">
            <table className="min-w-full divide-y divide-gray-200 text-sm">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-4 py-3 text-left font-medium text-gray-600">Username</th>
                  <th className="px-4 py-3 text-left font-medium text-gray-600">Email</th>
                  <th className="px-4 py-3 text-left font-medium text-gray-600">Role</th>
                  <th className="px-4 py-3 text-left font-medium text-gray-600">Status</th>
                  <th className="px-4 py-3 text-left font-medium text-gray-600">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100 bg-white">
                {loading ? (
                  <tr>
                    <td className="px-4 py-4 text-gray-500" colSpan={5}>Loading users...</td>
                  </tr>
                ) : users.length === 0 ? (
                  <tr>
                    <td className="px-4 py-4 text-gray-500" colSpan={5}>No users found.</td>
                  </tr>
                ) : (
                  users.map((user) => {
                    const normalizedStatus = user.status.toUpperCase();
                    const isBusy = busyUserId === user.id;
                    const isOwnerRow = user.role === 'OWNER';

                    return (
                      <tr key={user.id} className="hover:bg-gray-50">
                        <td className="px-4 py-3 text-gray-800 font-medium">{user.username}</td>
                        <td className="px-4 py-3 text-gray-700">{user.email}</td>
                        <td className="px-4 py-3 text-gray-700">{user.role}</td>
                        <td className="px-4 py-3">
                          <span className={`inline-flex items-center rounded-full border px-2.5 py-1 text-xs font-medium ${statusBadgeClass(user.status)}`}>
                            {user.status}
                          </span>
                        </td>
                        <td className="px-4 py-3">
                          <div className="flex flex-wrap items-center gap-2">
                            <Button
                              size="sm"
                              variant="outline"
                              onClick={() => runAction(user.id, 'activate')}
                              disabled={isBusy || normalizedStatus === 'ACTIVE' || isOwnerRow}
                            >
                              <UserCheck className="mr-1 h-3.5 w-3.5" /> Activate
                            </Button>
                            <Button
                              size="sm"
                              variant="outline"
                              onClick={() => runAction(user.id, 'deactivate')}
                              disabled={isBusy || normalizedStatus === 'INACTIVE' || isOwnerRow}
                            >
                              <UserX className="mr-1 h-3.5 w-3.5" /> Deactivate
                            </Button>
                            <Button
                              size="sm"
                              variant="destructive"
                              onClick={() => runAction(user.id, 'block')}
                              disabled={isBusy || normalizedStatus === 'BLOCKED' || isOwnerRow}
                            >
                              <ShieldAlert className="mr-1 h-3.5 w-3.5" /> Block
                            </Button>
                            <Button
                              size="sm"
                              variant="secondary"
                              onClick={() => runAction(user.id, 'unblock')}
                              disabled={isBusy || normalizedStatus !== 'BLOCKED' || isOwnerRow}
                            >
                              <ShieldCheck className="mr-1 h-3.5 w-3.5" /> Unblock
                            </Button>
                            <select
                              value={roleDrafts[user.id] ?? (user.role as UserRole)}
                              onChange={(e) => setRoleDrafts((prev) => ({
                                ...prev,
                                [user.id]: e.target.value as UserRole,
                              }))}
                              disabled={isBusy || isOwnerRow}
                              className="h-8 rounded-md border border-input bg-background px-2 text-xs"
                            >
                              <option value="CUSTOMER">CUSTOMER</option>
                              <option value="EMPLOYEE">EMPLOYEE</option>
                              <option value="OWNER">OWNER</option>
                            </select>
                            <Button
                              size="sm"
                              variant="outline"
                              onClick={() => runRoleChange(user.id)}
                              disabled={isBusy || isOwnerRow || (roleDrafts[user.id] ?? user.role) === user.role}
                            >
                              <UserCog className="mr-1 h-3.5 w-3.5" /> Apply Role
                            </Button>
                            <Button
                              size="sm"
                              variant="destructive"
                              onClick={() => runDelete(user.id)}
                              disabled={isBusy || isOwnerRow}
                            >
                              <Trash2 className="mr-1 h-3.5 w-3.5" /> Delete
                            </Button>
                          </div>
                        </td>
                      </tr>
                    );
                  })
                )}
              </tbody>
            </table>
          </div>

          <div className="flex items-center justify-between gap-3">
            <div className="text-sm text-gray-600">
              Page {Math.min(page + 1, Math.max(totalPages, 1))} of {Math.max(totalPages, 1)}
            </div>
            <div className="flex items-center gap-2">
              <Button
                variant="outline"
                size="sm"
                disabled={loading || page <= 0}
                onClick={() => loadUsers(page - 1, query, statusFilter, roleFilter)}
              >
                Previous
              </Button>
              <Button
                variant="outline"
                size="sm"
                disabled={loading || page + 1 >= totalPages}
                onClick={() => loadUsers(page + 1, query, statusFilter, roleFilter)}
              >
                Next
              </Button>
            </div>
          </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};

export default UsersPage;
