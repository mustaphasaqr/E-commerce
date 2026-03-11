/**
 * Order Module Types
 */

/**
 * Shipping address
 */
export interface ShippingAddress {
  id?: string;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  street: string;
  city: string;
  state: string;
  zipCode: string;
  country: string;
  isDefault?: boolean;
}

/**
 * Order item
 */
export interface OrderItem {
  id: string;
  productId: string;
  productName: string;
  quantity: number;
  price: number;
  discount?: number;
  subtotal: number;
}

/**
 * Order payment details
 */
export interface OrderPayment {
  id: string;
  method: 'CREDIT_CARD' | 'DEBIT_CARD' | 'PAYPAL' | 'WALLET';
  status: 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED' | 'CANCELLED';
  amount: number;
  transactionId?: string;
  processedAt?: string;
}

/**
 * Order tracking
 */
export interface OrderTracking {
  id: string;
  status: 'PENDING' | 'CONFIRMED' | 'PROCESSING' | 'SHIPPED' | 'DELIVERED' | 'CANCELLED';
  events: {
    status: string;
    timestamp: string;
    description: string;
  }[];
}

/**
 * Order
 */
export interface Order {
  id: string;
  userId: string;
  orderNumber: string;
  items: OrderItem[];
  shippingAddress: ShippingAddress;
  payment: OrderPayment;
  tracking: OrderTracking;
  subtotal: number;
  tax: number;
  shipping: number;
  discount: number;
  total: number;
  notes?: string;
  createdAt: string;
  updatedAt: string;
}

/**
 * Order state
 */
export interface OrderState {
  orders: Order[];
  selectedOrder: Order | null;
  loading: boolean;
  error: string | null;
  isCheckingOut: boolean;
  pagination: {
    page: number;
    pageSize: number;
    total: number;
  };
}

/**
 * Checkout request
 */
export interface CheckoutRequest {
  shippingAddress: ShippingAddress;
  paymentMethod: 'CREDIT_CARD' | 'DEBIT_CARD' | 'PAYPAL' | 'WALLET';
  paymentDetails?: Record<string, any>;
  notes?: string;
}

/**
 * Return request
 */
export interface ReturnRequest {
  orderId: string;
  itemIds?: string[];
  reason: string;
  description?: string;
}
