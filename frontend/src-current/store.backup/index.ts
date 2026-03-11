/**
 * Redux Store Configuration
 * Central store setup with all slices and middleware
 */

import { configureStore } from '@reduxjs/toolkit';
import { loggerMiddleware } from './middleware/logger';

// Import slices
import authReducer from '@auth/store/authSlice';
import productReducer from '@product/store/productSlice';
import cartReducer from '@cart/store/cartSlice';
import orderReducer from '@order/store/orderSlice';

/**
 * Configure Redux store
 */
export const store = configureStore({
  reducer: {
    auth: authReducer,
    products: productReducer,
    cart: cartReducer,
    orders: orderReducer,
  },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware({
      serializableCheck: {
        // Ignore certain action types that contain non-serializable values
        ignoredActions: ['auth/setUser'],
        ignoredPatterns: ['auth/.*'],
      },
    }).concat(loggerMiddleware),
  devTools: import.meta.env.DEV,
});

// Export types
export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;

export default store;
