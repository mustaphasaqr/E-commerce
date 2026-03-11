/**
 * Product Service
 * API calls for products
 */

import { apiClient } from '@shared/services/apiClient';
import { API_ENDPOINTS } from '@shared/utils/constants';
import type {
  Product,
  ProductsResponse,
  ProductReview,
  CreateReviewRequest,
  Category,
} from '../types/index';

/**
 * Product service class
 */
class ProductService {
  /**
   * Get all products with pagination
   */
  async getProducts(page = 1, pageSize = 20): Promise<ProductsResponse> {
    return apiClient.get(API_ENDPOINTS.products.list, {
      params: { page, pageSize },
    });
  }

  /**
   * Search products
   */
  async searchProducts(query: string, page = 1, pageSize = 20): Promise<ProductsResponse> {
    return apiClient.get(API_ENDPOINTS.products.search, {
      params: { q: query, page, pageSize },
    });
  }

  /**
   * Get product by ID
   */
  async getProductById(id: string): Promise<Product> {
    return apiClient.get(API_ENDPOINTS.products.getById(id));
  }

  /**
   * Get product categories
   */
  async getCategories(): Promise<Category[]> {
    return apiClient.get(API_ENDPOINTS.products.categories);
  }

  /**
   * Get product reviews
   */
  async getReviews(productId: string, page = 1, pageSize = 10): Promise<{
    data: ProductReview[];
    pagination: any;
  }> {
    return apiClient.get(API_ENDPOINTS.products.reviews(productId), {
      params: { page, pageSize },
    });
  }

  /**
   * Create product review
   */
  async createReview(data: CreateReviewRequest): Promise<ProductReview> {
    return apiClient.post(API_ENDPOINTS.products.createReview(data.productId), data);
  }

  /**
   * Get product recommendations
   */
  async getRecommendations(productId: string, limit = 5): Promise<Product[]> {
    return apiClient.get(API_ENDPOINTS.products.recommendations, {
      params: { productId, limit },
    });
  }

  /**
   * Get product filters
   */
  async getFilters(): Promise<{
    categories: Category[];
    priceRange: { min: number; max: number };
    ratings: number[];
  }> {
    return apiClient.get(API_ENDPOINTS.products.filters);
  }
}

// Export singleton instance
export const productService = new ProductService();

// Export class for custom instances if needed
export { ProductService };
