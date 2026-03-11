/**
 * useProductDetailsApi Hook
 * Direct API integration for single product details
 */

import { useApi } from '@shared/hooks/useApi';
import { apiClient } from '@shared/services/apiClient';
import { API_ENDPOINTS } from '@shared/utils/constants';
import type { Product, ReviewsResponse } from '../types/index';

/**
 * useProductDetailsApi hook - Fetch single product with caching
 */
export const useProductDetailsApi = (productId: string | null) => {
  const { data, error, status, isLoading, execute } = useApi<Product>(
    (id: string) => apiClient.get<Product>(API_ENDPOINTS.products.getById(id)),
    { autoExecute: false }
  );

  const fetchProduct = (id: string) => execute(id);

  return {
    product: data,
    loading: isLoading,
    error,
    status,
    fetchProduct,
    refetch: () => productId && fetchProduct(productId),
  };
};

/**
 * useProductReviewsApi hook - Fetch product reviews
 */
export const useProductReviewsApi = (productId: string) => {
  const { data, error, status, isLoading, execute } = useApi<ReviewsResponse>(
    async (page = 1) => 
      apiClient.get<ReviewsResponse>(API_ENDPOINTS.products.reviews(productId), {
        params: { page, pageSize: 10 },
      }),
    { autoExecute: false }
  );

  return {
    reviews: data?.data ?? [],
    pagination: data?.pagination,
    loading: isLoading,
    error,
    status,
    fetchReviews: (page = 1) => execute(page),
  };
};

/**
 * useProductRecommendationsApi hook - Get recommended products
 */
export const useProductRecommendationsApi = (productId: string) => {
  const { data, error, status, isLoading, execute } = useApi(
    async () => 
      apiClient.get(API_ENDPOINTS.products.recommendations, {
        params: { productId, limit: 5 },
      }),
    { autoExecute: !!productId }
  );

  return {
    recommendations: data ?? [],
    loading: isLoading,
    error,
    status,
    fetchRecommendations: () => execute(),
  };
};
