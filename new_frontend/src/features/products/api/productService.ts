import axios from '@/shared/api/axios'
import type {
	ProductDetail,
	ProductListItem,
	ProductReviewStats,
	ProductReviewsPage,
	SubmitReviewRequest,
} from '../types'

const toNumber = (value: unknown, fallback = 0): number => {
	if (typeof value === 'number' && Number.isFinite(value)) return value
	if (typeof value === 'string' && value.trim() !== '' && Number.isFinite(Number(value))) return Number(value)
	return fallback
}

const normalizeProduct = (raw: Record<string, unknown>): ProductListItem => ({
	id: String(raw.id ?? ''),
	sku: String(raw.sku ?? ''),
	name: String(raw.name ?? ''),
	price: toNumber(raw.price),
	currency: String(raw.currency ?? 'USD'),
	availableStock: toNumber(raw.availableStock),
	totalStock: toNumber(raw.totalStock ?? raw.availableStock),
	active: Boolean(raw.active ?? true),
	discontinued: Boolean(raw.discontinued ?? false),
})

export const productService = {
	async listProducts(): Promise<ProductListItem[]> {
		const response = await axios.get('/products')
		const items = Array.isArray(response.data) ? response.data : []
		return items.map((item) => normalizeProduct(item as Record<string, unknown>))
	},

	async getProductById(productId: string): Promise<ProductDetail> {
		const response = await axios.get(`/products/${productId}`)
		const normalized = normalizeProduct(response.data as Record<string, unknown>)
		return {
			...normalized,
			description: String((response.data as Record<string, unknown>).description ?? ''),
		}
	},

	async getProductReviewStats(productId: string): Promise<ProductReviewStats> {
		const response = await axios.get<ProductReviewStats>(`/products/${productId}/reviews/stats`)
		return response.data
	},

	async getProductReviews(productId: string, page = 0, size = 5, sortBy = 'MOST_HELPFUL'): Promise<ProductReviewsPage> {
		const response = await axios.get<ProductReviewsPage>(`/products/${productId}/reviews`, {
			params: { page, size, sortBy },
		})
		return response.data
	},

	async submitReview(productId: string, payload: SubmitReviewRequest): Promise<void> {
		await axios.post(`/products/${productId}/reviews`, payload)
	},

	async markReviewHelpful(productId: string, reviewId: number): Promise<void> {
		await axios.post(`/products/${productId}/reviews/${reviewId}/helpful`)
	},
}

export default productService
