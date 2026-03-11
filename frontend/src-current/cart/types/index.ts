/**
 * Cart Module Types
 */

import type { Product } from '@product/types/index';

/**
 * Cart item
 */
export interface CartItem {
  id: string;
  productId: string;
  product: Product;
  quantity: number;
  price: number;
  addedAt: string;
}

/**
 * Coupon
 */
export interface Coupon {
  id: string;
  code: string;
  discountType: 'PERCENTAGE' | 'FIXED';
  discountValue: number;
  maxUsage: number;
  usageCount: number;
  minOrderAmount?: number;
  expiresAt: string;
  isActive: boolean;
}

/**
 * Cart summary
 */
export interface CartSummary {
  subtotal: number;
  discount: number;
  tax: number;
  shipping: number;
  total: number;
  itemCount: number;
}

/**
 * Cart
 */
export interface Cart {
  id: string;
  items: CartItem[];
  coupons: Coupon[];
  summary: CartSummary;
  updatedAt: string;
}

/**
 * Cart state
 */
export interface CartState {
  cart: Cart | null;
  loading: boolean;
  error: string | null;
  isValidating: boolean;
}

/**
 * Add to cart request
 */
export interface AddToCartRequest {
  productId: string;
  quantity: number;
}

/**
 * Update cart item request
 */
export interface UpdateCartItemRequest {
  itemId: string;
  quantity: number;
}

/**
 * Apply coupon request
 */
export interface ApplyCouponRequest {
  cartId: string;
  couponCode: string;
}
