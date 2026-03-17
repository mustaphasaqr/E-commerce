import axios from '@/shared/api/axios'
import type { AddToCartRequest, CartDTO, UpdateCartItemRequest } from '../types'

export const cartService = {
	async getCart(): Promise<CartDTO> {
		const response = await axios.get<CartDTO>('/cart')
		return response.data
	},

	async addToCart(payload: AddToCartRequest): Promise<CartDTO> {
		const response = await axios.post<CartDTO>('/cart/items', payload)
		return response.data
	},

	async updateCartItem(payload: UpdateCartItemRequest): Promise<CartDTO> {
		const response = await axios.put<CartDTO>('/cart/items', payload)
		return response.data
	},

	async removeFromCart(productId: number | string): Promise<CartDTO> {
		const numericId = Number(productId)
		const response = await axios.delete<CartDTO>(`/cart/items/${numericId}`)
		return response.data
	},

	async clearCart(): Promise<CartDTO> {
		const response = await axios.delete<CartDTO>('/cart')
		return response.data
	},
}

export default cartService
