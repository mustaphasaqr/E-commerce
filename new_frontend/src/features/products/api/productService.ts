import axios from '@/shared/api/axios'
import type {
	ProductDetail,
	ProductListItem,
	ProductRecommendation,
	ProductReviewsPage,
	ProductReviewStats,
	ReviewSortBy,
	SubmitReviewPayload,
} from '../types'

export const productService = {
	async listProducts(): Promise<ProductListItem[]> {
		const response = await axios.get<ProductListItem[]>('/products')
		return Array.isArray(response.data) ? response.data : []
	},

	async getProductById(id: string): Promise<ProductDetail> {
		const response = await axios.get<ProductDetail>(`/products/${id}`)
		return response.data
	},

	async getProductBySku(sku: string): Promise<ProductDetail> {
		const response = await axios.get<ProductDetail>('/products', {
			params: { sku },
		})
		return response.data
	},

	async getTrendingProducts(limit = 10): Promise<ProductRecommendation[]> {
		const response = await axios.get<ProductRecommendation[]>('/products/recommendations/trending', {
			params: { limit },
		})
		return Array.isArray(response.data) ? response.data : []
	},

	async getPersonalizedRecommendations(limit = 10): Promise<ProductRecommendation[]> {
		const response = await axios.get<ProductRecommendation[]>('/products/recommendations/for-you', {
			params: { limit },
		})
		return Array.isArray(response.data) ? response.data : []
	},

	async getFrequentlyBoughtTogether(productId: string, limit = 5): Promise<ProductRecommendation[]> {
		const response = await axios.get<ProductRecommendation[]>(`/products/${productId}/recommendations/frequently-bought-together`, {
			params: { limit },
		})
		return Array.isArray(response.data) ? response.data : []
	},

	async getProductReviews(productId: string, page = 0, size = 5, sortBy: ReviewSortBy = 'MOST_HELPFUL'): Promise<ProductReviewsPage> {
		const response = await axios.get<ProductReviewsPage>(`/products/${productId}/reviews`, {
			params: { page, size, sortBy },
		})
		return response.data
	},

	async getProductReviewStats(productId: string): Promise<ProductReviewStats> {
		const response = await axios.get<ProductReviewStats>(`/products/${productId}/reviews/stats`)
		return response.data
	},

	async submitReview(productId: string, payload: SubmitReviewPayload): Promise<{ reviewId: number; message: string }> {
		const response = await axios.post<{ reviewId: number; message: string }>(`/products/${productId}/reviews`, payload)
		return response.data
	},

	async markReviewHelpful(productId: string, reviewId: number): Promise<void> {
		await axios.post(`/products/${productId}/reviews/${reviewId}/helpful`)
	},

	async updatePrice(id: string, newPrice: number, currencyCode: string): Promise<ProductDetail> {
		const response = await axios.put<ProductDetail>(`/products/${id}/price`, null, {
			params: { newPrice, currencyCode },
		})
		return response.data
	},

	async updateProductDetails(id: string, name: string, description: string): Promise<ProductDetail> {
		const response = await axios.put<ProductDetail>(`/products/${id}/details`, null, {
			params: { name, description },
		})
		return response.data
	},

	async activateProduct(id: string): Promise<ProductDetail> {
		const response = await axios.post<ProductDetail>(`/products/${id}/activate`)
		return response.data
	},

	async deactivateProduct(id: string): Promise<ProductDetail> {
		const response = await axios.post<ProductDetail>(`/products/${id}/deactivate`)
		return response.data
	},

	async discontinueProduct(id: string): Promise<ProductDetail> {
		const response = await axios.post<ProductDetail>(`/products/${id}/discontinue`)
		return response.data
	},

	async uploadProductImage(id: string, file: File): Promise<{ productId: string; imageUrl: string; message: string }> {
		const formData = new FormData()
		formData.append('file', file)
		const response = await axios.post<{ productId: string; imageUrl: string; message: string }>(`/products/${id}/images`, formData, {
			headers: {
				'Content-Type': 'multipart/form-data',
			},
		})
		return response.data
	},

	async deleteProductImage(id: string, imageUrl: string): Promise<{ productId?: string; message?: string; error?: string }> {
		const response = await axios.delete<{ productId?: string; message?: string; error?: string }>(`/products/${id}/images`, {
			params: { imageUrl },
		})
		return response.data
	},
}

export default productService
