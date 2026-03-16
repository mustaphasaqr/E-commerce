export interface OrderItemRequest {
	productId: string
	productName: string
	quantity: number
	price: number
}

export interface CreateOrderRequest {
	customerId: string
	items: OrderItemRequest[]
	paymentMethod?: string
	shippingCity?: string
	shippingState?: string
	shippingCountry?: string
	shippingZipCode?: string
	utmSource?: string
	utmCampaign?: string
	referrer?: string
	cartId?: number
}

export interface OrderListResponse {
	orderId: string
	customerId: string
	totalAmount: number
	status: string
	createdAt: string
}

export interface OrderItemResponse {
	productId: string
	productName: string
	quantity: number
	unitPrice: number
	subtotal: number
}

export interface OrderResponse {
	orderId: string
	customerId: string
	items: OrderItemResponse[]
	totalAmount: number
	status: string
	paymentMethod?: string
	createdAt: string
	updatedAt: string
	trackingNumber?: string
	carrier?: string
	deliveredAt?: string
	cancellationReason?: string
}
