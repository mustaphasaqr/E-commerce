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

export interface TaxLineItemRequest {
	productId: number
	productName: string
	unitPrice: number
	quantity: number
	taxCategory: 'STANDARD' | 'REDUCED' | 'ZERO' | 'EXEMPT'
}

export interface TaxCalculationRequest {
	orderId: number | null
	customerId: number
	subtotal: number
	shippingCountryCode: string
	billingCountryCode: string
	customerType: 'INDIVIDUAL' | 'BUSINESS'
	taxId?: string
	items: TaxLineItemRequest[]
}

export interface TaxBreakdown {
	productId: number
	productName: string
	amount: number
	tax: number
	taxRate: string
	taxCategory: string
}

export interface TaxCalculationResponse {
	subtotal: number
	taxAmount: number
	total: number
	taxRate: string
	jurisdiction: string
	taxType: string
	isTaxExempt: boolean
	breakdown: TaxBreakdown[]
}
