/**
 * User Types
 * Models for user domain (/api/v1/users/*)
 */

export interface UserProfile {
  id: string;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  avatar?: string;
  phone?: string;
  dateOfBirth?: string;
  gender?: 'MALE' | 'FEMALE' | 'OTHER';
  bio?: string;
  emailVerified: boolean;
  phoneVerified: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface UserPreferences {
  userId: string;
  theme: 'LIGHT' | 'DARK' | 'AUTO';
  language: string;
  currency: string;
  timezone: string;
  emailNotifications: boolean;
  pushNotifications: boolean;
  smsNotifications: boolean;
  marketingEmails: boolean;
  productRecommendations: boolean;
  orderUpdates: boolean;
  promotions: boolean;
  showProfile: boolean;
  allowMessaging: boolean;
}

export interface UserAddress {
  id: string;
  userId: string;
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
  label?: string;
  isDefault: boolean;
  isShippingDefault: boolean;
  isBillingDefault: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface UpdateProfileRequest {
  firstName?: string;
  lastName?: string;
  phone?: string;
  dateOfBirth?: string;
  gender?: 'MALE' | 'FEMALE' | 'OTHER';
  bio?: string;
  avatar?: string;
}

export interface UpdatePreferencesRequest {
  theme?: 'LIGHT' | 'DARK' | 'AUTO';
  language?: string;
  currency?: string;
  timezone?: string;
  emailNotifications?: boolean;
  pushNotifications?: boolean;
  smsNotifications?: boolean;
  marketingEmails?: boolean;
  productRecommendations?: boolean;
  orderUpdates?: boolean;
  promotions?: boolean;
  showProfile?: boolean;
  allowMessaging?: boolean;
}

export interface CreateAddressRequest {
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
  label?: string;
  isDefault?: boolean;
  isShippingDefault?: boolean;
  isBillingDefault?: boolean;
}

export interface UpdateAddressRequest {
  firstName?: string;
  lastName?: string;
  email?: string;
  phone?: string;
  company?: string;
  addressLine1?: string;
  addressLine2?: string;
  city?: string;
  state?: string;
  postalCode?: string;
  country?: string;
  label?: string;
  isDefault?: boolean;
  isShippingDefault?: boolean;
  isBillingDefault?: boolean;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
}

export interface VerifyEmailRequest {
  token: string;
}

export interface RequestEmailVerificationRequest {
  email: string;
}

export interface WishlistItem {
  id: string;
  userId: string;
  productId: string;
  addedAt: string;
}

export interface UserStats {
  totalOrders: number;
  totalSpent: number;
  totalSavings: number;
  averageOrderValue: number;
  memberSince: string;
  loyaltyPoints: number;
  loyaltyTier: 'BRONZE' | 'SILVER' | 'GOLD' | 'PLATINUM';
}

export interface RecentActivity {
  id: string;
  userId: string;
  type: 'ORDER_PLACED' | 'PRODUCT_VIEWED' | 'ITEM_ADDED_TO_CART' | 'REVIEW_POSTED' | 'ADDRESS_ADDED';
  description: string;
  metadata?: Record<string, any>;
  timestamp: string;
}

export interface UserDocument {
  id: string;
  userId: string;
  type: 'INVOICE' | 'RECEIPT' | 'RETURN_LABEL' | 'SHIPPING_LABEL';
  reference: string;
  url: string;
  uploadedAt: string;
  expiresAt?: string;
}
