/**
 * Order Types
 * Models for order domain (/api/v1/orders/*)
 */

import { Product } from '@product/types/product.types';

export enum OrderStatus {
  PENDING = 'PENDING',
  CONFIRMED = 'CONFIRMED',
  PROCESSING = 'PROCESSING',
  SHIPPED = 'SHIPPED',
  DELIVERED = 'DELIVERED',
  CANCELLED = 'CANCELLED',
  RETURNED = 'RETURNED',
}

export enum PaymentStatus {
  PENDING = 'PENDING',
  PROCESSING = 'PROCESSING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
  REFUNDED = 'REFUNDED',
  PARTIAL_REFUND = 'PARTIAL_REFUND',
}

export enum PaymentMethod {
  CREDIT_CARD = 'CREDIT_CARD',
  DEBIT_CARD = 'DEBIT_CARD',
  PAYPAL = 'PAYPAL',
  BANK_TRANSFER = 'BANK_TRANSFER',
  DIGITAL_WALLET = 'DIGITAL_WALLET',
}

export enum ShippingMethod {
  STANDARD = 'STANDARD',
  EXPRESS = 'EXPRESS',
  OVERNIGHT = 'OVERNIGHT',
  PICKUP = 'PICKUP',
}

export interface OrderItem {
  id: string;
  orderId: string;
  productId: string;
  product?: Product;
  quantity: number;
  unitPrice: number;
  discount?: number;
  tax: number;
  total: number;
}

export interface Order {
  id: string;
  userId: string;
  orderNumber: string;
  status: OrderStatus;
  paymentStatus: PaymentStatus;
  items: OrderItem[];
  subtotal: number;
  taxAmount: number;
  shippingCost: number;
  discountAmount: number;
  total: number;
  shippingAddress: Address;
  billingAddress?: Address;
  shippingMethod: ShippingMethod;
  paymentMethod: PaymentMethod;
  promoCode?: string;
  notes?: string;
  trackingNumber?: string;
  carrier?: string;
  estimatedDelivery?: string;
  actualDelivery?: string;
  createdAt: string;
  updatedAt: string;
  completedAt?: string;
  cancelledAt?: string;
}

export interface Address {
  id?: string;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  company?: string;
  addressLine1: string;
  addressLine2?: string;
  city: string;
  state: string;
  postalCode: string;
  country: string;
  isDefault?: boolean;
}

export interface CreateOrderRequest {
  items: {
    productId: string;
    quantity: number;
  }[];
  shippingAddressId?: string;
  shippingAddress?: Address;
  billingAddressId?: string;
  billingAddress?: Address;
  shippingMethod: ShippingMethod;
  paymentMethod: PaymentMethod;
  promoCode?: string;
  notes?: string;
}

export interface CreateCheckoutSessionRequest {
  cartId: string;
  shippingAddressId?: string;
  shippingAddress?: Address;
  billingAddressId?: string;
  billingAddress?: Address;
  shippingMethod: ShippingMethod;
  promoCode?: string;
}

export interface CheckoutSession {
  id: string;
  orderId: string;
  sessionToken: string;
  expiresAt: string;
  paymentIntentId: string;
  clientSecret: string;
  status: 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED';
}

export interface PaymentConfirmRequest {
  orderId: string;
  paymentIntentId: string;
  paymentMethodId: string;
}

export interface CancelOrderRequest {
  reason: string;
  idempotencyKey: string;
}

export interface TrackingInfo {
  orderId: string;
  trackingNumber: string;
  carrier: string;
  status: string;
  events: TrackingEvent[];
}

export interface TrackingEvent {
  timestamp: string;
  status: string;
  location: string;
  description: string;
}

export interface OrderHistory {
  orders: Order[];
  total: number;
  page: number;
  pageSize: number;
  hasNext: boolean;
  hasPrev: boolean;
}

export interface RefundRequest {
  orderId: string;
  reason: string;
  amount?: number;
  items?: string[];
}

export interface Receipt {
  orderId: string;
  orderNumber: string;
  purchaseDate: string;
  items: OrderItem[];
  subtotal: number;
  tax: number;
  shipping: number;
  discount: number;
  total: number;
  shippingAddress: Address;
  paymentMethod: PaymentMethod;
}
