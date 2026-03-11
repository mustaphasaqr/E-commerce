/**
 * Cart Service
 * API calls for cart operations
 */

import { apiClient } from '@shared/services/apiClient';
import { API_ENDPOINTS } from '@shared/utils/constants';
import type { Cart, AddToCartRequest, UpdateCartItemRequest, ApplyCouponRequest } from '../types/index';

/**
 * Cart service class
 */
class CartService {
  /**
   * Get current user's cart
   */
  async getCart(): Promise<Cart> {
    return apiClient.get(API_ENDPOINTS.cart.get);
  }

  /**
   * Add item to cart
   */
  async addToCart(data: AddToCartRequest): Promise<Cart> {
    return apiClient.post(API_ENDPOINTS.cart.add, data);
  }

  /**
   * Update cart item quantity
   */
  async updateCartItem(data: UpdateCartItemRequest): Promise<Cart> {
    return apiClient.put(API_ENDPOINTS.cart.update(data.itemId), {
      quantity: data.quantity,
    });
  }

  /**
   * Remove item from cart
   */
  async removeFromCart(itemId: string): Promise<Cart> {
    return apiClient.delete(API_ENDPOINTS.cart.remove(itemId));
  }

  /**
   * Clear entire cart
   */
  async clearCart(): Promise<void> {
    return apiClient.post(API_ENDPOINTS.cart.clear, {});
  }

  /**
   * Validate cart
   */
  async validateCart(): Promise<{ isValid: boolean; message?: string }> {
    return apiClient.post(API_ENDPOINTS.cart.validate, {});
  }

  /**
   * Apply coupon to cart
   */
  async applyCoupon(data: ApplyCouponRequest): Promise<Cart> {
    return apiClient.post(API_ENDPOINTS.cart.applyCoupon, {
      couponCode: data.couponCode,
    });
  }

  /**
   * Remove coupon from cart
   */
  async removeCoupon(couponId: string): Promise<Cart> {
    return apiClient.delete(API_ENDPOINTS.cart.removeCoupon(couponId));
  }
}

// Export singleton instance
export const cartService = new CartService();

// Export class for custom instances if needed
export { CartService };
