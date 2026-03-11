/**
 * Order Service
 * API calls for orders
 */

import { apiClient } from '@shared/services/apiClient';
import { API_ENDPOINTS } from '@shared/utils/constants';
import type { Order, CheckoutRequest, ReturnRequest } from '../types/index';

/**
 * Order service class
 */
class OrderService {
  /**
   * Get user's orders
   */
  async getOrders(page = 1, pageSize = 10): Promise<{
    data: Order[];
    pagination: any;
  }> {
    return apiClient.get(API_ENDPOINTS.orders.list, {
      params: { page, pageSize },
    });
  }

  /**
   * Get order by ID
   */
  async getOrderById(id: string): Promise<Order> {
    return apiClient.get(API_ENDPOINTS.orders.getById(id));
  }

  /**
   * Create order from cart
   */
  async createOrder(): Promise<Order> {
    return apiClient.post(API_ENDPOINTS.orders.create, {});
  }

  /**
   * Checkout - Create and process payment
   */
  async checkout(data: CheckoutRequest): Promise<Order> {
    return apiClient.post(API_ENDPOINTS.orders.checkout, data);
  }

  /**
   * Cancel order
   */
  async cancelOrder(id: string): Promise<Order> {
    return apiClient.post(API_ENDPOINTS.orders.cancel(id), {});
  }

  /**
   * Request return for order
   */
  async requestReturn(data: ReturnRequest): Promise<{ returnId: string; status: string }> {
    return apiClient.post(API_ENDPOINTS.orders.return(data.orderId), {
      itemIds: data.itemIds,
      reason: data.reason,
      description: data.description,
    });
  }

  /**
   * Track order
   */
  async trackOrder(id: string): Promise<{
    status: string;
    estimatedDelivery?: string;
    tracking?: any;
  }> {
    return apiClient.get(API_ENDPOINTS.orders.track(id));
  }

  /**
   * Get order invoice
   */
  async getInvoice(id: string): Promise<void> {
    return apiClient.download(API_ENDPOINTS.orders.invoice(id), `invoice-${id}.pdf`);
  }
}

// Export singleton instance
export const orderService = new OrderService();

// Export class for custom instances if needed
export { OrderService };
