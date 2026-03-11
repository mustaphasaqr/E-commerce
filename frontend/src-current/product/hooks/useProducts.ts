/**
 * useProducts Hook
 * Custom hook for accessing product state and actions
 */

import { useCallback } from 'react';
import { useAppDispatch, useAppSelector } from '@store/hooks';
import {
  fetchProductsStart,
  fetchProductsSuccess,
  fetchProductsFailure,
  fetchProductStart,
  fetchProductSuccess,
  fetchProductFailure,
  fetchCategoriesSuccess,
  fetchReviewsSuccess,
  setFilters,
  setPagination,
} from '../store/productSlice';
import { productService } from '../services/productService';
import type { ProductFilter } from '../types/index';

/**
 * useProducts hook
 */
export const useProducts = () => {
  const dispatch = useAppDispatch();
  const products = useAppSelector((state) => state.products);

  /**
   * Fetch products
   */
  const fetchProducts = useCallback(
    async (page = 1, pageSize = 20) => {
      try {
        dispatch(fetchProductsStart());
        const response = await productService.getProducts(page, pageSize);
        dispatch(
          fetchProductsSuccess({
            items: response.data,
            page,
            pageSize,
            total: response.pagination?.total || 0,
          })
        );
        return response;
      } catch (error) {
        const message = error instanceof Error ? error.message : 'Failed to fetch products';
        dispatch(fetchProductsFailure(message));
        throw error;
      }
    },
    [dispatch]
  );

  /**
   * Fetch single product
   */
  const fetchProduct = useCallback(
    async (id: string) => {
      // Check cache first
      if (products.cache[id]) {
        dispatch(fetchProductSuccess(products.cache[id]));
        return products.cache[id];
      }

      try {
        dispatch(fetchProductStart());
        const product = await productService.getProductById(id);
        dispatch(fetchProductSuccess(product));
        return product;
      } catch (error) {
        const message = error instanceof Error ? error.message : 'Failed to fetch product';
        dispatch(fetchProductFailure(message));
        throw error;
      }
    },
    [dispatch, products.cache]
  );

  /**
   * Search products
   */
  const searchProducts = useCallback(
    async (query: string, page = 1) => {
      try {
        dispatch(fetchProductsStart());
        const response = await productService.searchProducts(query, page);
        dispatch(
          fetchProductsSuccess({
            items: response.data,
            page,
            pageSize: 20,
            total: response.pagination?.total || 0,
          })
        );
        return response;
      } catch (error) {
        const message = error instanceof Error ? error.message : 'Search failed';
        dispatch(fetchProductsFailure(message));
        throw error;
      }
    },
    [dispatch]
  );

  /**
   * Fetch categories
   */
  const fetchCategories = useCallback(async () => {
    try {
      const categories = await productService.getCategories();
      dispatch(fetchCategoriesSuccess(categories));
      return categories;
    } catch (error) {
      console.error('Failed to fetch categories:', error);
      throw error;
    }
  }, [dispatch]);

  /**
   * Fetch reviews for a product
   */
  const fetchReviews = useCallback(
    async (productId: string, page = 1) => {
      try {
        const response = await productService.getReviews(productId, page);
        dispatch(fetchReviewsSuccess(response.data));
        return response;
      } catch (error) {
        console.error('Failed to fetch reviews:', error);
        throw error;
      }
    },
    [dispatch]
  );

  /**
   * Update filters
   */
  const updateFilters = useCallback(
    (filters: ProductFilter) => {
      dispatch(setFilters(filters));
    },
    [dispatch]
  );

  /**
   * Update pagination
   */
  const updatePagination = useCallback(
    (page: number, pageSize: number) => {
      dispatch(setPagination({ page, pageSize }));
    },
    [dispatch]
  );

  return {
    items: products.items,
    selectedProduct: products.selectedProduct,
    reviews: products.reviews,
    categories: products.categories,
    loading: products.loading,
    error: products.error,
    filters: products.filters,
    pagination: products.pagination,
    fetchProducts,
    fetchProduct,
    searchProducts,
    fetchCategories,
    fetchReviews,
    updateFilters,
    updatePagination,
  };
};
