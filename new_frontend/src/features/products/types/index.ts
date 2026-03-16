export interface ProductListItem {
	id: string
	sku: string
	name: string
	price: number
	currency: string
	availableStock: number
	active: boolean
}

export interface ProductDetail extends ProductListItem {
	description: string
	totalStock: number
	reservedStock: number
	visible: boolean
	availableForPurchase: boolean
	discontinued: boolean
}

export interface ProductQueryParams {
	page: number
	pageSize: number
	search: string
	category: string
	sort: 'name-asc' | 'name-desc' | 'price-asc' | 'price-desc' | 'stock-desc'
}

export interface ProductRecommendation {
	productId: string
	productName: string
	price: number
	imageUrl?: string | null
	confidence?: number
	reason?: string
}

export type ReviewSortBy = 'NEWEST' | 'OLDEST' | 'HIGHEST_RATING' | 'LOWEST_RATING' | 'MOST_HELPFUL'

export interface ProductReviewSummary {
	id: number
	customerId: string
	customerName: string
	rating: number
	title: string
	reviewText: string
	isVerifiedPurchase: boolean
	helpfulCount: number
	notHelpfulCount: number
	adminResponse?: string | null
	createdAt: string
}

export interface ProductReviewsPage {
	reviews: ProductReviewSummary[]
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

export interface SubmitReviewPayload {
	productId: string
	customerId: string
	customerName: string
	orderId: number
	rating: number
	title: string
	reviewText: string
}
