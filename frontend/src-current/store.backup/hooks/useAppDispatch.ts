/**
 * useAppDispatch Hook
 * Typed Redux dispatch hook for the application
 */

import { useDispatch } from 'react-redux';
import type { AppDispatch } from '../index';

/**
 * Use throughout your app instead of plain `useDispatch`
 */
export const useAppDispatch = () => useDispatch<AppDispatch>();
