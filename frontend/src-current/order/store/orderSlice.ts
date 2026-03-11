/**
 * Order Redux Slice
 * State management for orders
 */

import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import type { OrderState, Order } from '../types/index';

const initialState: OrderState = {
  orders: [],
  selectedOrder: null,
  loading: false,
  error: null,
  isCheckingOut: false,
  pagination: {
    page: 1,
    pageSize: 10,
    total: 0,
  },
};

/**
 * Order slice
 */
const orderSlice = createSlice({
  name: 'orders',
  initialState,
  reducers: {
    // Fetch orders
    fetchOrdersStart: (state) => {
      state.loading = true;
      state.error = null;
    },
    fetchOrdersSuccess: (
      state,
      action: PayloadAction<{
        orders: Order[];
        page: number;
        pageSize: number;
        total: number;
      }>
    ) => {
      state.loading = false;
      state.orders = action.payload.orders;
      state.pagination = {
        page: action.payload.page,
        pageSize: action.payload.pageSize,
        total: action.payload.total,
      };
    },
    fetchOrdersFailure: (state, action: PayloadAction<string>) => {
      state.loading = false;
      state.error = action.payload;
    },

    // Fetch single order
    fetchOrderStart: (state) => {
      state.loading = true;
      state.error = null;
    },
    fetchOrderSuccess: (state, action: PayloadAction<Order>) => {
      state.loading = false;
      state.selectedOrder = action.payload;
    },
    fetchOrderFailure: (state, action: PayloadAction<string>) => {
      state.loading = false;
      state.error = action.payload;
    },

    // Checkout
    checkoutStart: (state) => {
      state.isCheckingOut = true;
      state.error = null;
    },
    checkoutSuccess: (state, action: PayloadAction<Order>) => {
      state.isCheckingOut = false;
      state.selectedOrder = action.payload;
      state.orders.unshift(action.payload);
    },
    checkoutFailure: (state, action: PayloadAction<string>) => {
      state.isCheckingOut = false;
      state.error = action.payload;
    },

    // Cancel order
    cancelOrderSuccess: (state, action: PayloadAction<Order>) => {
      const index = state.orders.findIndex((o) => o.id === action.payload.id);
      if (index !== -1) {
        state.orders[index] = action.payload;
      }
      if (state.selectedOrder?.id === action.payload.id) {
        state.selectedOrder = action.payload;
      }
    },

    // Request return
    requestReturnSuccess: (_state, _action: PayloadAction<string>) => {
      // Return ID is in action payload
    },

    // Track order
    trackOrderSuccess: (state, action: PayloadAction<Order>) => {
      if (state.selectedOrder?.id === action.payload.id) {
        state.selectedOrder = action.payload;
      }
    },

    // Clear selected order
    clearSelectedOrder: (state) => {
      state.selectedOrder = null;
    },
  },
});

// Export actions
export const {
  fetchOrdersStart,
  fetchOrdersSuccess,
  fetchOrdersFailure,
  fetchOrderStart,
  fetchOrderSuccess,
  fetchOrderFailure,
  checkoutStart,
  checkoutSuccess,
  checkoutFailure,
  cancelOrderSuccess,
  requestReturnSuccess,
  trackOrderSuccess,
  clearSelectedOrder,
} = orderSlice.actions;

// Export reducer
export default orderSlice.reducer;
