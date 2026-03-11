/**
 * useCart Hook
 * Custom hook for accessing cart state and actions
 */

import { useCallback } from 'react';
import { useAppDispatch, useAppSelector } from '@store/hooks';
import {
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
} from '../store/cartSlice';
import { cartService } from '../services/cartService';
import type { AddToCartRequest, UpdateCartItemRequest, ApplyCouponRequest } from '../types/index';

/**
 * useCart hook
 */
export const useCart = () => {
  const dispatch = useAppDispatch();
  const cart = useAppSelector((state) => state.cart);

  /**
   * Fetch cart
   */
  const fetchCart = useCallback(async () => {
    try {
      dispatch(fetchCartStart());
      const result = await cartService.getCart();
      dispatch(fetchCartSuccess(result));
      return result;
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Failed to fetch cart';
      dispatch(fetchCartFailure(message));
      throw error;
    }
  }, [dispatch]);

  /**
   * Add item to cart
   */
  const addItem = useCallback(
    async (data: AddToCartRequest) => {
      try {
        dispatch(addItemStart());
        const result = await cartService.addToCart(data);
        dispatch(addItemSuccess(result));
        return result;
      } catch (error) {
        const message = error instanceof Error ? error.message : 'Failed to add item';
        dispatch(addItemFailure(message));
        throw error;
      }
    },
    [dispatch]
  );

  /**
   * Update item quantity
   */
  const updateItem = useCallback(
    async (data: UpdateCartItemRequest) => {
      try {
        dispatch(updateItemStart());
        const result = await cartService.updateCartItem(data);
        dispatch(updateItemSuccess(result));
        return result;
      } catch (error) {
        const message = error instanceof Error ? error.message : 'Failed to update item';
        dispatch(updateItemFailure(message));
        throw error;
      }
    },
    [dispatch]
  );

  /**
   * Remove item from cart
   */
  const removeItem = useCallback(
    async (itemId: string) => {
      try {
        dispatch(removeItemStart());
        const result = await cartService.removeFromCart(itemId);
        dispatch(removeItemSuccess(result));
        return result;
      } catch (error) {
        const message = error instanceof Error ? error.message : 'Failed to remove item';
        dispatch(removeItemFailure(message));
        throw error;
      }
    },
    [dispatch]
  );

  /**
   * Clear cart
   */
  const clearCart = useCallback(async () => {
    try {
      await cartService.clearCart();
      dispatch(clearCartSuccess());
    } catch (error) {
      console.error('Failed to clear cart:', error);
      throw error;
    }
  }, [dispatch]);

  /**
   * Apply coupon
   */
  const applyCoupon = useCallback(
    async (data: ApplyCouponRequest) => {
      try {
        const result = await cartService.applyCoupon(data);
        dispatch(applyCouponSuccess(result));
        return result;
      } catch (error) {
        throw error;
      }
    },
    [dispatch]
  );

  /**
   * Remove coupon
   */
  const removeCoupon = useCallback(
    async (couponId: string) => {
      try {
        const result = await cartService.removeCoupon(couponId);
        dispatch(removeCouponSuccess(result));
        return result;
      } catch (error) {
        throw error;
      }
    },
    [dispatch]
  );

  /**
   * Validate cart
   */
  const validateCart = useCallback(async () => {
    try {
      dispatch(validateCartStart());
      const result = await cartService.validateCart();
      dispatch(validateCartSuccess());
      return result;
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Cart validation failed';
      dispatch(validateCartFailure(message));
      throw error;
    }
  }, [dispatch]);

  return {
    cart: cart.cart,
    loading: cart.loading,
    error: cart.error,
    isValidating: cart.isValidating,
    fetchCart,
    addItem,
    updateItem,
    removeItem,
    clearCart,
    applyCoupon,
    removeCoupon,
    validateCart,
  };
};
