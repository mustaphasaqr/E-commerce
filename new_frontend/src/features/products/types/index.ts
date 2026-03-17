export interface ProductListItem {
	id: string
	sku: string
	name: string
	price: number
	currency: string
	availableStock: number
	totalStock: number
	active: boolean
	discontinued?: boolean
}

export interface ProductDetail extends ProductListItem {
	description?: string
}

export interface ProductReview {
	id: number
	customerId: string
	customerName: string
	rating: number
	title?: string
	reviewText: string
	isVerifiedPurchase: boolean
	helpfulCount: number
	notHelpfulCount: number
	adminResponse?: string | null
	createdAt: string
}

export interface ProductReviewsPage {
	reviews: ProductReview[]
	totalReviews: number
	page: number
	size: number
}

export interface ProductReviewStats {
	averageRating: number
	totalReviews: number
	ratingDistribution: Record<string, number>
	verifiedPurchasePercentage: number
}

export interface SubmitReviewRequest {
	productId: string
	customerId: string
	customerName: string
	orderId: number
	rating: number
	title?: string
	reviewText: string
}
