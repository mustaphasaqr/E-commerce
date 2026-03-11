/**
 * useOrder Hook
 * Custom hook for accessing order state and actions
 */

import { useCallback } from 'react';
import { useAppDispatch, useAppSelector } from '@store/hooks';
import {
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
} from '../store/orderSlice';
import { orderService } from '../services/orderService';
import type { CheckoutRequest, ReturnRequest } from '../types/index';

/**
 * useOrder hook
 */
export const useOrder = () => {
  const dispatch = useAppDispatch();
  const orders = useAppSelector((state) => state.orders);

  /**
   * Fetch user's orders
   */
  const fetchOrders = useCallback(
    async (page = 1, pageSize = 10) => {
      try {
        dispatch(fetchOrdersStart());
        const response = await orderService.getOrders(page, pageSize);
        dispatch(
          fetchOrdersSuccess({
            orders: response.data,
            page,
            pageSize,
            total: response.pagination?.total || 0,
          })
        );
        return response;
      } catch (error) {
        const message = error instanceof Error ? error.message : 'Failed to fetch orders';
        dispatch(fetchOrdersFailure(message));
        throw error;
      }
    },
    [dispatch]
  );

  /**
   * Fetch single order
   */
  const fetchOrder = useCallback(
    async (id: string) => {
      try {
        dispatch(fetchOrderStart());
        const order = await orderService.getOrderById(id);
        dispatch(fetchOrderSuccess(order));
        return order;
      } catch (error) {
        const message = error instanceof Error ? error.message : 'Failed to fetch order';
        dispatch(fetchOrderFailure(message));
        throw error;
      }
    },
    [dispatch]
  );

  /**
   * Checkout
   */
  const checkout = useCallback(
    async (data: CheckoutRequest) => {
      try {
        dispatch(checkoutStart());
        const order = await orderService.checkout(data);
        dispatch(checkoutSuccess(order));
        return order;
      } catch (error) {
        const message = error instanceof Error ? error.message : 'Checkout failed';
        dispatch(checkoutFailure(message));
        throw error;
      }
    },
    [dispatch]
  );

  /**
   * Cancel order
   */
  const cancelOrder = useCallback(
    async (id: string) => {
      try {
        const order = await orderService.cancelOrder(id);
        dispatch(cancelOrderSuccess(order));
        return order;
      } catch (error) {
        console.error('Failed to cancel order:', error);
        throw error;
      }
    },
    [dispatch]
  );

  /**
   * Request return
   */
  const requestReturn = useCallback(
    async (data: ReturnRequest) => {
      try {
        const result = await orderService.requestReturn(data);
        dispatch(requestReturnSuccess(result.returnId));
        return result;
      } catch (error) {
        console.error('Failed to request return:', error);
        throw error;
      }
    },
    [dispatch]
  );

  /**
   * Track order
   */
  const trackOrder = useCallback(
    async (id: string) => {
      try {
        const tracking = await orderService.trackOrder(id);
        return tracking;
      } catch (error) {
        console.error('Failed to track order:', error);
        throw error;
      }
    },
    []
  );

  /**
   * Download invoice
   */
  const downloadInvoice = useCallback(async (id: string) => {
    try {
      await orderService.getInvoice(id);
    } catch (error) {
      console.error('Failed to download invoice:', error);
      throw error;
    }
  }, []);

  return {
    orders: orders.orders,
    selectedOrder: orders.selectedOrder,
    loading: orders.loading,
    error: orders.error,
    isCheckingOut: orders.isCheckingOut,
    pagination: orders.pagination,
    fetchOrders,
    fetchOrder,
    checkout,
    cancelOrder,
    requestReturn,
    trackOrder,
    downloadInvoice,
  };
};
