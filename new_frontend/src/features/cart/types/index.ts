export interface CartItem {
	productId: string
	productName: string
	quantity: number
	price: number
	subtotal: number
}

export interface CartDTO {
	id: number | null
	userId: string | null
	sessionId: string
	items: CartItem[]
	totalAmount: number
	status: string
	totalItems: number
}

export interface AddToCartRequest {
	productId: string
	quantity: number
}

export interface UpdateCartItemRequest {
	productId: string
	quantity: number
}
