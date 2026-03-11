/**
 * useAuth Hook
 * Custom hook for accessing auth state and actions
 */

import { useCallback } from 'react';
import { useAppDispatch, useAppSelector } from '@store/hooks';
import {
  loginStart,
  loginSuccess,
  loginFailure,
  registerStart,
  registerSuccess,
  registerFailure,
  logout,
  clearError,
} from '../store/authSlice';
import { authService } from '../services/authService';
import type { LoginRequest, RegisterRequest } from '../types/index';

/**
 * useAuth hook
 */
export const useAuth = () => {
  const dispatch = useAppDispatch();
  const auth = useAppSelector((state) => state.auth);

  /**
   * Login handler
   */
  const login = useCallback(
    async (credentials: LoginRequest) => {
      try {
        dispatch(loginStart());
        const response = await authService.login(credentials);

        // Store tokens and user
        localStorage.setItem('auth_token', response.accessToken);
        localStorage.setItem('refresh_token', response.refreshToken);
        localStorage.setItem('user_id', response.user.id);
        localStorage.setItem('auth_user', JSON.stringify(response.user));

        dispatch(loginSuccess(response));
        return response;
      } catch (error) {
        const message = error instanceof Error ? error.message : 'Login failed';
        dispatch(loginFailure(message));
        throw error;
      }
    },
    [dispatch]
  );

  /**
   * Register handler
   */
  const register = useCallback(
    async (data: RegisterRequest) => {
      try {
        dispatch(registerStart());
        const response = await authService.register(data);

        // Store tokens and user
        localStorage.setItem('auth_token', response.accessToken);
        localStorage.setItem('refresh_token', response.refreshToken);
        localStorage.setItem('user_id', response.user.id);
        localStorage.setItem('auth_user', JSON.stringify(response.user));

        dispatch(registerSuccess(response));
        return response;
      } catch (error) {
        const message = error instanceof Error ? error.message : 'Registration failed';
        dispatch(registerFailure(message));
        throw error;
      }
    },
    [dispatch]
  );

  /**
   * Logout handler
   */
  const handleLogout = useCallback(async () => {
    try {
      await authService.logout();
    } catch (error) {
      console.error('Logout error:', error);
    } finally {
      authService.clearTokens();
      dispatch(logout());
    }
  }, [dispatch]);

  return {
    user: auth.user,
    tokens: auth.tokens,
    isLoading: auth.isLoading,
    error: auth.error,
    isAuthenticated: auth.isAuthenticated,
    login,
    register,
    logout: handleLogout,
    clearError: () => dispatch(clearError()),
  };
};
