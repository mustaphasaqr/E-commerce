/**
 * Cart Redux Slice
 * State management for shopping cart
 */

import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import type { CartState, Cart } from '../types/index';

const initialState: CartState = {
  cart: null,
  loading: false,
  error: null,
  isValidating: false,
};

/**
 * Cart slice
 */
const cartSlice = createSlice({
  name: 'cart',
  initialState,
  reducers: {
    // Fetch cart
    fetchCartStart: (state) => {
      state.loading = true;
      state.error = null;
    },
    fetchCartSuccess: (state, action: PayloadAction<Cart>) => {
      state.loading = false;
      state.cart = action.payload;
      state.error = null;
    },
    fetchCartFailure: (state, action: PayloadAction<string>) => {
      state.loading = false;
      state.error = action.payload;
    },

    // Add item
    addItemStart: (state) => {
      state.loading = true;
      state.error = null;
    },
    addItemSuccess: (state, action: PayloadAction<Cart>) => {
      state.loading = false;
      state.cart = action.payload;
    },
    addItemFailure: (state, action: PayloadAction<string>) => {
      state.loading = false;
      state.error = action.payload;
    },

    // Update item
    updateItemStart: (state) => {
      state.loading = true;
      state.error = null;
    },
    updateItemSuccess: (state, action: PayloadAction<Cart>) => {
      state.loading = false;
      state.cart = action.payload;
    },
    updateItemFailure: (state, action: PayloadAction<string>) => {
      state.loading = false;
      state.error = action.payload;
    },

    // Remove item
    removeItemStart: (state) => {
      state.loading = true;
      state.error = null;
    },
    removeItemSuccess: (state, action: PayloadAction<Cart>) => {
      state.loading = false;
      state.cart = action.payload;
    },
    removeItemFailure: (state, action: PayloadAction<string>) => {
      state.loading = false;
      state.error = action.payload;
    },

    // Clear cart
    clearCartSuccess: (state) => {
      state.cart = null;
      state.error = null;
    },

    // Apply coupon
    applyCouponSuccess: (state, action: PayloadAction<Cart>) => {
      state.cart = action.payload;
    },

    // Remove coupon
    removeCouponSuccess: (state, action: PayloadAction<Cart>) => {
      state.cart = action.payload;
    },

    // Validate cart
    validateCartStart: (state) => {
      state.isValidating = true;
    },
    validateCartSuccess: (state) => {
      state.isValidating = false;
    },
    validateCartFailure: (state, action: PayloadAction<string>) => {
      state.isValidating = false;
      state.error = action.payload;
    },
  },
});

// Export actions
export const {
  fetchCartStart,
  fetchCartSuccess,
  fetchCartFailure,
  addItemStart,
  addItemSuccess,
  addItemFailure,
  updateItemStart,
  updateItemSuccess,
  updateItemFailure,
  removeItemStart,
  removeItemSuccess,
  removeItemFailure,
  clearCartSuccess,
  applyCouponSuccess,
  removeCouponSuccess,
  validateCartStart,
  validateCartSuccess,
  validateCartFailure,
} = cartSlice.actions;

// Export reducer
export default cartSlice.reducer;
