import axios from '@/shared/api/axios'
import type { CreateOrderRequest, OrderListResponse, OrderResponse } from '../types'

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
}

export default orderService
