/**
 * Product Module Types
 */

/**
 * Product category
 */
export interface Category {
  id: string;
  name: string;
  slug: string;
  description: string;
  image?: string;
}

/**
 * Product rating
 */
export interface ProductRating {
  average: number;
  count: number;
  distribution: {
    [key in 1 | 2 | 3 | 4 | 5]: number;
  };
}

/**
 * Product entity
 */
export interface Product {
  id: string;
  name: string;
  slug: string;
  description: string;
  price: number;
  originalPrice?: number;
  discount?: number;
  image: string;
  images: string[];
  category: Category;
  stock: number;
  rating: ProductRating;
  sku: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

/**
 * Product review
 */
export interface ProductReview {
  id: string;
  productId: string;
  userId: string;
  userName: string;
  rating: number;
  title: string;
  content: string;
  helpful: number;
  createdAt: string;
}

/**
 * Product filter
 */
export interface ProductFilter {
  categoryId?: string;
  priceMin?: number;
  priceMax?: number;
  rating?: number;
  inStock?: boolean;
  search?: string;
  sort?: 'name' | 'price' | 'rating' | 'newest';
  sortOrder?: 'asc' | 'desc';
}

/**
 * Products pagination response
 */
export interface ProductsResponse {
  data: Product[];
  pagination: {
    page: number;
    pageSize: number;
    total: number;
    totalPages: number;
  };
}

/**\n * Product reviews response
 */
export interface ReviewsResponse {
  data: ProductReview[];
  pagination: {
    page: number;
    pageSize: number;
    total: number;
    totalPages: number;
  };
}

/**
 * Product state
 */
export interface ProductState {
  items: Product[];
  selectedProduct: Product | null;
  reviews: ProductReview[];
  categories: Category[];
  loading: boolean;
  error: string | null;
  filters: ProductFilter;
  pagination: {
    page: number;
    pageSize: number;
    total: number;
  };
  cache: Record<string, Product>;
}

/**
 * Create review request
 */
export interface CreateReviewRequest {
  productId: string;
  rating: number;
  title: string;
  content: string;
}
