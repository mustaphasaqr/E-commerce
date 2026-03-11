/**
 * Product Types
 * Models for product domain (/api/v1/products/*)
 */

export interface Product {
  id: string;
  name: string;
  description: string;
  price: number;
  originalPrice?: number;
  discount?: number;
  images: ProductImage[];
  category: Category;
  stock: number;
  sku: string;
  rating: number;
  reviewCount: number;
  isAvailable: boolean;
  isFeatured: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface ProductImage {
  id: string;
  url: string;
  altText: string;
  isPrimary: boolean;
  sortOrder: number;
}

export interface Category {
  id: string;
  name: string;
  slug: string;
  description?: string;
  parentId?: string;
  image?: string;
}

export interface Review {
  id: string;
  productId: string;
  userId: string;
  rating: number;
  title: string;
  comment: string;
  verified: boolean;
  helpfulCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface ProductFilter {
  categories?: string[];
  priceRange?: {
    min: number;
    max: number;
  };
  rating?: number;
  search?: string;
  onlyAvailable?: boolean;
  onlyFeatured?: boolean;
  brands?: string[];
  colors?: string[];
  sizes?: string[];
}

export interface ProductCreateRequest {
  name: string;
  description: string;
  price: number;
  originalPrice?: number;
  categoryId: string;
  stock: number;
  sku: string;
  images: {
    url: string;
    altText: string;
    isPrimary: boolean;
  }[];
}

export interface ProductUpdateRequest {
  name?: string;
  description?: string;
  price?: number;
  originalPrice?: number;
  categoryId?: string;
  stock?: number;
  sku?: string;
  isAvailable?: boolean;
  isFeatured?: boolean;
}

export interface ReviewCreateRequest {
  rating: number;
  title: string;
  comment: string;
}

export interface SearchProductsRequest {
  query: string;
  filter?: ProductFilter;
  page?: number;
  pageSize?: number;
  sort?: string;
}

export interface ProductListResponse {
  items: Product[];
  total: number;
  page: number;
  pageSize: number;
  hasNext: boolean;
  hasPrev: boolean;
}

export interface RecommendedProduct {
  id: string;
  name: string;
  price: number;
  image: ProductImage;
  reason: 'POPULAR' | 'TRENDING' | 'SIMILAR' | 'FREQUENTLY_BOUGHT';
}

export type SortOption = 'relevance' | 'price_asc' | 'price_desc' | 'newest' | 'rating' | 'popular';
