/**
 * useProductsApi Hook
 * Direct API integration using useApi for product listing
 */

import { useApi } from '@shared/hooks/useApi';
import { apiClient } from '@shared/services/apiClient';
import { API_ENDPOINTS } from '@shared/utils/constants';
import type { ProductsResponse } from '../types/index';

/**
 * useProductsApi hook - Direct API fetching with automatic state management
 */
export const useProductsApi = () => {
  const {
    data,
    error,
    status,
    isLoading,
    execute,
    refetch,
  } = useApi<ProductsResponse>(
    async (page = 1, pageSize = 20, filters = {}) =>
      apiClient.get<ProductsResponse>(API_ENDPOINTS.products.list, {
        params: { page, pageSize, ...filters },
      }),
    { autoExecute: false }
  );

  return {
    products: data?.data ?? [],
    pagination: data?.pagination,
    loading: isLoading,
    error,
    status,
    fetchProducts: (page = 1, pageSize = 20, filters = {}) =>
      execute(page, pageSize, filters),
    refetch,
  };
};

/**
 * useProductSearchApi hook - Search products with automatic debouncing
 */
export const useProductSearchApi = () => {
  const { data, error, status, isLoading, execute } = useApi<ProductsResponse>(
    async (query: string, page = 1) =>
      apiClient.get<ProductsResponse>(API_ENDPOINTS.products.search, {
        params: { q: query, page, pageSize: 20 },
      }),
    { autoExecute: false }
  );

  return {
    results: data?.data ?? [],
    pagination: data?.pagination,
    loading: isLoading,
    error,
    status,
    searchProducts: (query: string, page = 1) =>
      execute(query, page),
  };
};

/**
 * useProductCategoriesApi hook - Fetch product categories
 */
export const useProductCategoriesApi = () => {
  const { data, error, status, isLoading, refetch } = useApi<any>(
    async () => apiClient.get(API_ENDPOINTS.products.categories),
    { autoExecute: true }
  );

  return {
    categories: data ?? [],
    loading: isLoading,
    error,
    status,
    refetch,
  };
};
