/**
 * Cart Types
 * Models for cart domain (/api/v1/cart/*)
 */

import { Product } from '@product/types/product.types';

export interface CartItem {
  id: string;
  productId: string;
  product?: Product;
  quantity: number;
  price: number;
  discount?: number;
  addedAt: string;
  updatedAt: string;
}

export interface Cart {
  id: string;
  userId: string;
  items: CartItem[];
  totalItems: number;
  subtotal: number;
  taxAmount: number;
  shippingCost: number;
  discountAmount: number;
  total: number;
  promoCode?: PromoCode;
  lastUpdated: string;
  expiresAt?: string;
}

export interface CartItemRequest {
  productId: string;
  quantity: number;
}

export interface UpdateCartItemRequest {
  quantity: number;
}

export interface ApplyPromoCodeRequest {
  code: string;
}

export interface PromoCode {
  id: string;
  code: string;
  description: string;
  discountType: 'PERCENTAGE' | 'FIXED';
  discountValue: number;
  maxDiscount?: number;
  minOrderAmount?: number;
  maxUsageCount?: number;
  usageCount: number;
  expiresAt: string;
  isActive: boolean;
  applicableProducts?: string[];
}

export interface CartPersistenceState {
  items: CartItem[];
  lastSyncedAt: string;
  isDirty: boolean;
  syncErrors?: string[];
}

export interface CartValidationResult {
  isValid: boolean;
  errors: CartValidationError[];
  warnings: CartValidationWarning[];
}

export interface CartValidationError {
  itemId: string;
  productId: string;
  code: 'OUT_OF_STOCK' | 'PRODUCT_NOT_FOUND' | 'PRICE_CHANGED' | 'INVALID_QUANTITY';
  message: string;
}

export interface CartValidationWarning {
  itemId: string;
  productId: string;
  code: 'LOW_STOCK' | 'PRICE_INCREASED' | 'DISCOUNT_EXPIRED';
  message: string;
  previousValue?: any;
  currentValue?: any;
}

export interface CartSyncRequest {
  items: CartItem[];
  lastSyncedAt: string;
}

export interface ClearCartRequest {
  confirmed: boolean;
}

export type CartChangeEvent = 
  | { type: 'ITEM_ADDED'; item: CartItem }
  | { type: 'ITEM_REMOVED'; itemId: string }
  | { type: 'ITEM_QUANTITY_CHANGED'; itemId: string; quantity: number }
  | { type: 'PROMO_APPLIED'; promoCode: PromoCode }
  | { type: 'PROMO_REMOVED' }
  | { type: 'CART_CLEARED' }
  | { type: 'CART_SYNCED'; cart: Cart };
