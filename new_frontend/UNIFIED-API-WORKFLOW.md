# **Unified API Implementation Workflow**

## **Overview**
This is the **single workflow** you'll follow for every API feature. It combines explicit typing (type safety) with practical test-driven development.

---

## **The 5-Step Workflow**

### **Step 1: Define Data Types**
Create TypeScript interfaces that match backend response shapes.

**File:** `src/features/{feature}/types/index.ts`

```typescript
// Example: Products feature
export interface Product {
  id: string
  name: string
  price: number
  description: string
  stock: number
  createdAt: string
}

export interface ProductRequest {
  name: string
  price: number
  description: string
  stock: number
}

export interface ApiResponse<T> {
  status: 'success' | 'error'
  data: T
  message?: string
}
```

**Why:** TypeScript catches mismatches between frontend expectations and backend responses at compile time.

---

### **Step 2: Write API Service Tests**
Test that API service methods **return the correct types** and handle errors.

**File:** `src/features/{feature}/api/productService.test.ts`

```typescript
import { describe, it, expect, vi } from 'vitest'
import api from '@/shared/api/axios'
import { getProducts, getProductById } from './productService'
import { Product } from '../types'

// Mock axios
vi.mock('@/shared/api/axios')

describe('Product API Service', () => {
  it('should fetch products and return typed array', async () => {
    const mockData: Product[] = [
      { id: '1', name: 'Laptop', price: 999, description: '...', stock: 5, createdAt: '2024-01-01' }
    ]
    
    vi.mocked(api.get).mockResolvedValue({ data: mockData })
    
    const result = await getProducts()
    
    expect(result).toEqual(mockData)
    expect(result[0].price).toBe(999) // Type-safe access
  })

  it('should handle API errors gracefully', async () => {
    vi.mocked(api.get).mockRejectedValue(new Error('Network error'))
    
    expect(getProducts()).rejects.toThrow('Network error')
  })
})
```

**Why:** Tests verify the contract before writing UI code. You catch backend API changes immediately.

---

### **Step 3: Build Typed API Service**
Implement API calls with explicit parameter and return types.

**File:** `src/features/{feature}/api/productService.ts`

```typescript
import api from '@/shared/api/axios'
import { Product, ProductRequest, ApiResponse } from '../types'

/**
 * Fetch all products from backend
 * @returns Promise<Product[]> - Typed array of products
 * @throws Error if API call fails
 */
export async function getProducts(): Promise<Product[]> {
  const response = await api.get<ApiResponse<Product[]>>('/api/products')
  return response.data.data
}

/**
 * Fetch single product by ID
 * @param id - Product ID
 * @returns Promise<Product> - Single typed product
 */
export async function getProductById(id: string): Promise<Product> {
  const response = await api.get<ApiResponse<Product>>(`/api/products/${id}`)
  return response.data.data
}

/**
 * Create new product
 * @param data - Product data to create
 * @returns Promise<Product> - Created product with ID
 */
export async function createProduct(data: ProductRequest): Promise<Product> {
  const response = await api.post<ApiResponse<Product>>('/api/products', data)
  return response.data.data
}
```

**Why:** 
- Parameters are typed (TypeScript catches wrong args)
- Return types are explicit (IDE autocomplete knows Product shape)
- Axios monitoring logs all calls automatically

---

### **Step 4: Build Typed Custom Hook**
Create React hook wrapping the API service with loading/error state.

**File:** `src/features/{feature}/hooks/useProducts.ts`

```typescript
import { useState, useEffect } from 'react'
import { Product } from '../types'
import { getProducts } from '../api/productService'

interface UseProductsReturn {
  products: Product[] // Typed array
  loading: boolean
  error: string | null
  refetch: () => Promise<void>
}

/**
 * Custom hook - Fetch and manage products
 *
 * Usage:
 * const { products, loading, error } = useProducts()
 *
 * Behavior:
 * - Fetches on mount
 * - Logs to console via Axios interceptors
 * - Handles loading/error states automatically
 */
export function useProducts(): UseProductsReturn {
  const [products, setProducts] = useState<Product[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const refetch = async () => {
    setLoading(true)
    setError(null)
    try {
      const data = await getProducts()
      setProducts(data)
      console.log(`✅ Loaded ${data.length} products`)
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Failed to load products'
      setError(message)
      console.error(`❌ Product load failed: ${message}`)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    refetch()
  }, [])

  return { products, loading, error, refetch }
}

/**
 * Hook for single product
 */
export function useProduct(id: string) {
  const [product, setProduct] = useState<Product | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!id) return
    
    const fetch = async () => {
      try {
        const data = await getProductById(id)
        setProduct(data)
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Failed to load product')
      } finally {
        setLoading(false)
      }
    }

    fetch()
  }, [id])

  return { product, loading, error }
}
```

**Why:**
- Hook return type is explicit (IDE knows about `products`, `loading`, `error`)
- Axios logs all network calls automatically
- Loading/error states happen in one place
- Easy to test in isolation

---

### **Step 5: Build Typed React Component**
Use the hook and types in your UI component.

**File:** `src/features/{feature}/components/ProductList.tsx`

```typescript
import { useProducts } from '../hooks/useProducts'
import { Product } from '../types'

/**
 * ProductList Component
 *
 * Features:
 * - Displays all products
 * - Shows loading spinner while fetching
 * - Shows error message if fetch fails
 * - Includes refresh button
 */
export function ProductList() {
  const { products, loading, error, refetch } = useProducts()

  if (loading) {
    return <div className="flex justify-center p-8">⏳ Loading products...</div>
  }

  if (error) {
    return (
      <div className="rounded-lg bg-red-50 p-4 text-red-800 flex justify-between">
        <span>❌ Error: {error}</span>
        <button
          onClick={refetch}
          className="text-red-600 underline hover:text-red-800"
        >
          Retry
        </button>
      </div>
    )
  }

  // TypeScript knows `products` is Product[] - full autocomplete
  return (
    <div className="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-3">
      {products.map((product: Product) => (
        <div key={product.id} className="border rounded-lg p-4">
          <h3 className="font-bold text-lg">{product.name}</h3>
          <p className="text-gray-600">{product.description}</p>
          <div className="mt-4 flex justify-between items-center">
            <span className="text-2xl font-bold">${product.price}</span>
            <span className="text-sm text-gray-500">Stock: {product.stock}</span>
          </div>
        </div>
      ))}
    </div>
  )
}

/**
 * ProductCard Component - Reusable single product card
 */
export function ProductCard({ product }: { product: Product }) {
  return (
    <div className="border rounded-lg p-4">
      <h4 className="font-bold">{product.name}</h4>
      <p className="text-sm text-gray-600">{product.description}</p>
      <p className="mt-2 font-bold">${product.price}</p>
    </div>
  )
}
```

**Why:**
- Component uses typed hook return values
- IDE provides autocomplete for `product.name`, `product.price`, etc.
- Built-in UI states (loading, error) make it production-ready
- Axios logs every network call automatically

---

## **Complete Data Flow (Typed End-to-End)**

```
User clicks "Load Products"
    ↓
Component calls useProducts() hook
    ↓
Hook calls productService.getProducts()
    ↓
Service calls api.get('/api/products')
    ↓
Axios interceptor logs: 📤 API Request: GET /api/products
    ↓
Backend returns: { status: 'success', data: [{ id: '1', name: 'Laptop', price: 999, ... }] }
    ↓
Axios response interceptor logs: ✅ API Success: 200 /api/products
    ↓
Service returns Promise<Product[]> with types
    ↓
Hook receives typed array, calls setProducts()
    ↓
Component receives { products: Product[], loading, error }
    ↓
Component renders with full TypeScript autocomplete: product.price ✓
```

**Every step is typed. Every API call is logged. Beautiful.**

---

## **Key Principles**

| Principle | Why |
|-----------|-----|
| **Types First** | Define shapes before implementation |
| **Tests Before Code** | Verify contract before UI code |
| **Single Service Method** | One function per API endpoint |
| **Hooks Manage State** | React component stays focused on UI |
| **Axios Logs All** | See every network call, immediately |
| **Errors Handled** | Loading/error states built-in |
| **TypeScript Autocomplete** | IDE knows all properties and methods |

---

## **When to Use This Workflow**

✅ **Always use this workflow for:**
- Backend API integration
- Data fetching (GET)
- Data mutations (POST, PUT, DELETE)
- Any feature requiring API calls

✅ **Files created in this exact order:**
1. `types/index.ts` - Data shapes
2. `api/{service}.test.ts` - Tests
3. `api/{service}.ts` - Implementation
4. `hooks/use{Feature}.ts` - React hook
5. `components/{Feature}.tsx` - UI

---

## **Examples by Feature**

### Products Feature
1. `types/index.ts` → Product interface
2. `api/productService.test.ts` → Test getProducts()
3. `api/productService.ts` → Implementation
4. `hooks/useProducts.ts` → React hook
5. `components/ProductList.tsx` → UI

### Cart Feature
1. `types/index.ts` → CartItem, CartState
2. `api/cartService.test.ts` → Test
3. `api/cartService.ts` → Add/remove/update items
4. `hooks/useCart.ts` → Zustand + API combined
5. `components/CartSummary.tsx` → UI

### Auth Feature
1. `types/index.ts` → LoginRequest, AuthResponse
2. `api/authService.test.ts` → Test login/register
3. `api/authService.ts` → Login and register calls
4. `hooks/useLogin.ts` → Login form handling
5. `components/LoginForm.tsx` → Login UI

---

## **This IS Both Previous Approaches**

- **Approach 1 (Basic):** Steps 1-5 form the workflow ✓
- **Approach 2 (Contract-First):** Types are defined first, then tested ✓

**They're the same workflow. The document you see is the unified version with step numbers.**

