import axios from '@/shared/api/axios'
import rawAxios from 'axios'

export interface InitiatePaymentRequest {
  orderId: string
  paymentMethod: 'VISA' | 'MASTERCARD' | 'MADA'
  customerEmail: string
  shopperResultUrl?: string
}

export interface PaymentCheckoutResponse {
  success: boolean
  checkoutId: string
  redirectUrl: string
  expiresInSeconds: number
  message?: string
  error?: string
}

export interface PaymentVerifyResponse {
  success: boolean
  status: 'SUCCESS' | 'FAILED' | 'PENDING' | 'CANCELLED' | string
  transactionId?: string
  message?: string
  error?: string
}

const LAST_CHECKOUT_ID_KEY = 'lastPaymentCheckoutId'

export const getLastCheckoutId = (): string => {
  if (typeof window === 'undefined' || !localStorage) return ''
  return localStorage.getItem(LAST_CHECKOUT_ID_KEY) || ''
}

export const setLastCheckoutId = (checkoutId: string): void => {
  if (typeof window === 'undefined' || !localStorage) return
  if (!checkoutId.trim()) return
  localStorage.setItem(LAST_CHECKOUT_ID_KEY, checkoutId.trim())
}

export const paymentService = {
  async initiateCheckout(payload: InitiatePaymentRequest): Promise<PaymentCheckoutResponse> {
    const response = await axios.post<PaymentCheckoutResponse>('/payments/checkout', payload)
    return response.data
  },

  async verifyCheckout(checkoutId: string): Promise<PaymentVerifyResponse> {
    const response = await axios.post<PaymentVerifyResponse>('/payments/verify', { checkoutId })
    return response.data
  },

  async paymentHealth(): Promise<Record<string, string>> {
    const response = await axios.get<Record<string, string>>('/payments/health')
    return response.data
  },

  async webhookVerify(checkoutId: string): Promise<PaymentVerifyResponse> {
    const response = await rawAxios.get<PaymentVerifyResponse>('/api/webhooks/payment/verify', {
      params: { checkoutId },
    })
    return response.data
  },

  async webhookHealth(): Promise<Record<string, string>> {
    const response = await rawAxios.get<Record<string, string>>('/api/webhooks/payment/health')
    return response.data
  },
}

export default paymentService
