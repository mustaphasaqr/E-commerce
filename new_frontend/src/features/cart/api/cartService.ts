import axios from '@/shared/api/axios'
import type { AddToCartRequest, CartDTO, UpdateCartItemRequest } from '../types'

export const cartService = {
	async getCart(): Promise<CartDTO> {
		const response = await axios.get<CartDTO>('/cart')
		return response.data
	},

	async addToCart(request: AddToCartRequest): Promise<CartDTO> {
		const response = await axios.post<CartDTO>('/cart/items', request)
		return response.data
	},

	async updateCartItem(request: UpdateCartItemRequest): Promise<CartDTO> {
		const response = await axios.put<CartDTO>('/cart/items', request)
		return response.data
	},

	async removeFromCart(productId: string): Promise<CartDTO> {
		const response = await axios.delete<CartDTO>(`/cart/items/${productId}`)
		return response.data
	},

	async clearCart(): Promise<CartDTO> {
		const response = await axios.delete<CartDTO>('/cart')
		return response.data
	},
}

export default cartService
