import axios from '@/shared/api/axios'
import type {
	CreateOrderRequest,
	OrderListResponse,
	OrderResponse,
	TaxCalculationRequest,
	TaxCalculationResponse,
} from '../types'

export const orderService = {
	async listOrders(): Promise<OrderListResponse[]> {
		const response = await axios.get<OrderListResponse[]>('/orders')
		return Array.isArray(response.data) ? response.data : []
	},

	async getOrder(orderId: string): Promise<OrderResponse> {
		const response = await axios.get<OrderResponse>(`/orders/${orderId}`)
		return response.data
	},

	async createOrder(payload: CreateOrderRequest, idempotencyKey?: string): Promise<OrderResponse> {
		const response = await axios.post<OrderResponse>('/orders', payload, {
			headers: idempotencyKey
				? {
						'Idempotency-Key': idempotencyKey,
					}
				: undefined,
		})
		return response.data
	},

	async cancelOrder(orderId: string, reason: string): Promise<OrderResponse> {
		const response = await axios.post<OrderResponse>(`/orders/${orderId}/cancel`, null, {
			params: { reason },
		})
		return response.data
	},

	async payOrder(orderId: string, payload: {
		paymentMethod: string
		paymentToken: string
		amount: number
	}): Promise<OrderResponse> {
		const response = await axios.post<OrderResponse>(`/orders/${orderId}/pay`, null, {
			params: payload,
		})
		return response.data
	},

	async shipOrder(orderId: string, payload: {
		trackingNumber: string
		carrier: string
	}): Promise<OrderResponse> {
		const response = await axios.post<OrderResponse>(`/orders/${orderId}/ship`, null, {
			params: payload,
		})
		return response.data
	},

	async deliverOrder(orderId: string): Promise<OrderResponse> {
		const response = await axios.post<OrderResponse>(`/orders/${orderId}/deliver`)
		return response.data
	},

	async calculateTax(payload: TaxCalculationRequest): Promise<TaxCalculationResponse> {
		const response = await axios.post<TaxCalculationResponse>('/orders/calculate-tax', payload)
		return response.data
	},
}

export default orderService
