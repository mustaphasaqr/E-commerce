/**
 * Product Redux Slice
 * State management for products
 */

import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import type { ProductState, Product, ProductReview, Category, ProductFilter } from '../types/index';

const initialState: ProductState = {
  items: [],
  selectedProduct: null,
  reviews: [],
  categories: [],
  loading: false,
  error: null,
  filters: {},
  pagination: {
    page: 1,
    pageSize: 20,
    total: 0,
  },
  cache: {},
};

/**
 * Product slice
 */
const productSlice = createSlice({
  name: 'products',
  initialState,
  reducers: {
    // Fetch products
    fetchProductsStart: (state) => {
      state.loading = true;
      state.error = null;
    },
    fetchProductsSuccess: (
      state,
      action: PayloadAction<{
        items: Product[];
        page: number;
        pageSize: number;
        total: number;
      }>
    ) => {
      state.loading = false;
      state.items = action.payload.items;
      state.pagination = {
        page: action.payload.page,
        pageSize: action.payload.pageSize,
        total: action.payload.total,
      };
      // Cache products
      action.payload.items.forEach((product) => {
        state.cache[product.id] = product;
      });
    },
    fetchProductsFailure: (state, action: PayloadAction<string>) => {
      state.loading = false;
      state.error = action.payload;
    },

    // Fetch single product
    fetchProductStart: (state) => {
      state.loading = true;
      state.error = null;
    },
    fetchProductSuccess: (state, action: PayloadAction<Product>) => {
      state.loading = false;
      state.selectedProduct = action.payload;
      state.cache[action.payload.id] = action.payload;
    },
    fetchProductFailure: (state, action: PayloadAction<string>) => {
      state.loading = false;
      state.error = action.payload;
    },

    // Fetch categories
    fetchCategoriesSuccess: (state, action: PayloadAction<Category[]>) => {
      state.categories = action.payload;
    },

    // Fetch reviews
    fetchReviewsSuccess: (state, action: PayloadAction<ProductReview[]>) => {
      state.reviews = action.payload;
    },

    // Update filters
    setFilters: (state, action: PayloadAction<ProductFilter>) => {
      state.filters = action.payload;
    },

    // Set pagination
    setPagination: (
      state,
      action: PayloadAction<{ page: number; pageSize: number }>
    ) => {
      state.pagination.page = action.payload.page;
      state.pagination.pageSize = action.payload.pageSize;
    },

    // Clear cache
    clearCache: (state) => {
      state.cache = {};
    },
  },
});

// Export actions
export const {
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
  clearCache,
} = productSlice.actions;

// Export reducer
export default productSlice.reducer;
