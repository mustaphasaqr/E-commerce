export interface CartItem {
	productId: number
	productName: string
	quantity: number
	price: number
	subtotal: number
}

export interface CartDTO {
	id: number | null
	userId: number | null
	sessionId: string
	items: CartItem[]
	totalAmount: number
	status: string
	totalItems: number
}

export interface AddToCartRequest {
	productId: number | string
	quantity: number
}

export interface UpdateCartItemRequest {
	productId: number | string
	quantity: number
}
