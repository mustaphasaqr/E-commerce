/**
 * useAppSelector Hook
 * Typed Redux selector hook for the application
 */

import { useSelector, type TypedUseSelectorHook } from 'react-redux';
import type { RootState } from '../index';

/**
 * Use throughout your app instead of plain `useSelector`
 */
export const useAppSelector: TypedUseSelectorHook<RootState> = useSelector;
