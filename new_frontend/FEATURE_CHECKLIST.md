# Frontend Feature Implementation Checklist

**IMPORTANT: Print this checklist and pin it near your desk. Review BEFORE starting any feature.**

This checklist ensures every feature is built reliably with proper architecture, testing, and monitoring.

---

## 15 Essential Concerns

### 1. **API Contract Definition** ✅
Before writing ANY code, define the exact shape of API responses.

```typescript
// /src/features/[feature]/types/index.ts
export interface Product {
  id: string
  name: string
  price: number
  category: string
  description: string
  stock: number
  createdAt: string
}
```

**Checklist:**
- [ ] Interface/type created for API response
- [ ] All required fields documented
- [ ] Field types match backend exactly (number vs string for IDs?)
- [ ] Nested objects defined (e.g., User inside Order)
- [ ] Array types specified (e.g., Product[])

**Why:** Prevents "undefined" errors and type mismatches at runtime.

---

### 2. **API Service with Axios** ✅
Create service functions that call backend endpoints BEFORE building UI.

```typescript
// /src/features/[feature]/api/[feature]Service.ts
import axios from '@/shared/api/axios'
import type { Product } from '../types'

export const productService = {
  async getProducts(): Promise<Product[]> {
    const response = await axios.get<Product[]>('/products')
    return response.data
  },

  async getProduct(id: string): Promise<Product> {
    const response = await axios.get<Product>(`/products/${id}`)
    return response.data
  }
}
```

**Checklist:**
- [ ] Service function created for each endpoint
- [ ] Correct HTTP method (GET, POST, PUT, DELETE)
- [ ] Correct endpoint URL (matches backend /api/v1/...)
- [ ] TypeScript types on response (Promise<Type>)
- [ ] Error handling for failed requests (let Axios interceptors catch it)
- [ ] Axios logs visible in DevTools Console (📤Request, ✅Success, ❌Error)

**Why:** Tests service before building UI against it. Axios monitoring shows all API calls.

---

### 3. **Service Tests** ✅
Prove the service can actually call the backend correctly.

```typescript
// /src/features/[feature]/api/[feature]Service.test.ts
import { describe, it, expect, vi, beforeEach } from 'vitest'
import axios from '@/shared/api/axios'
import { productService } from './productService'

vi.mock('@/shared/api/axios')

describe('productService', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('getProducts returns Product[]', async () => {
    const mockData: Product[] = [
      { id: '1', name: 'Product 1', price: 29.99, category: 'electronics', description: 'Test', stock: 10, createdAt: '2024-01-01' }
    ]
    vi.mocked(axios.get).mockResolvedValueOnce({ data: mockData })

    const result = await productService.getProducts()

    expect(axios.get).toHaveBeenCalledWith('/products')
    expect(result).toEqual(mockData)
    expect(result[0]).toHaveProperty('id', 'name', 'price')
  })
})
```

**Checklist:**
- [ ] Test file created (**.test.ts or **.spec.ts)
- [ ] Axios mocked (not calling real backend)
- [ ] Test verifies correct endpoint called
- [ ] Test verifies response has correct type
- [ ] Test covers error cases (404, 500, network error)
- [ ] Test runs: `npm run test` passes ✅

**Why:** Catches endpoint mistakes before building UI on top.

---

### 4. **Zod Validation Schema** ✅
Define validation rules so user input won't crash backend or UI.

```typescript
// /src/shared/validation/product.schema.ts
import { z } from 'zod'

export const ProductFilterSchema = z.object({
  search: z.string().min(0).max(100).optional(),
  category: z.string().optional(),
  minPrice: z.number().min(0).optional(),
  maxPrice: z.number().min(0).optional(),
  sortBy: z.enum(['name', 'price', 'newest']).optional()
})

export type ProductFilter = z.infer<typeof ProductFilterSchema>
```

**Checklist:**
- [ ] Schema created for user input / API params
- [ ] Validation rules defined (min, max, pattern, enum)
- [ ] Type inferred from schema (z.infer)
- [ ] Schema used in form component (react-hook-form integration)
- [ ] Validation tested: `ProductFilterSchema.parse(data)` doesn't throw

**Why:** Prevents invalid data from reaching backend or crashing UI.

---

### 5. **Global State Management (Zustand)** ✅
Create store for shared feature state (auth, cart, user, filters).

```typescript
// /src/features/[feature]/store/index.ts
import { create } from 'zustand'
import type { Product } from '../types'

interface ProductState {
  products: Product[]
  loading: boolean
  error: string | null
  setProducts: (products: Product[]) => void
  setLoading: (loading: boolean) => void
  setError: (error: string | null) => void
}

export const useProductStore = create<ProductState>((set) => ({
  products: [],
  loading: false,
  error: null,
  setProducts: (products) => set({ products }),
  setLoading: (loading) => set({ loading }),
  setError: (error) => set({ error })
}))
```

**Checklist:**
- [ ] Store created (useXxxStore)
- [ ] State shape defined (interface)
- [ ] Actions defined (setters, async operations)
- [ ] Store used in hook (see #6)
- [ ] DevTools integration (add middleware for debugging if needed)
- [ ] No duplicate state (not in both component AND store)

**Why:** Shared state accessible from any component without prop drilling.

---

### 6. **Custom Hook for Data Fetching** ✅
Hook = service + store + lifecycle. Handles loading, error, refresh.

```typescript
// /src/features/[feature]/hooks/useProducts.ts
import { useEffect } from 'react'
import { productService } from '../api/productService'
import { useProductStore } from '../store'

export function useProducts() {
  const { products, loading, error, setProducts, setLoading, setError } = useProductStore()

  const fetchProducts = async () => {
    setLoading(true)
    try {
      const data = await productService.getProducts()
      setProducts(data)
      // 📤 Axios automatically logged to console
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to fetch')
      // ❌ Axios error also logged to console
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchProducts()
  }, [])

  return { products, loading, error, refetch: fetchProducts }
}
```

**Checklist:**
- [ ] Hook function created (useXxx)
- [ ] Calls service (productService.getProducts)
- [ ] Updates store (setProducts, setLoading, setError)
- [ ] useEffect for initial fetch
- [ ] Error handling with try/catch
- [ ] Refetch method exposed for manual refresh
- [ ] Monitored in console (check for 📤✅❌ logs when used)

**Why:** Encapsulates data fetching logic, reusable across components.

---

### 7. **Loading State UI** ✅
Show skeleton/spinner while data loads, don't show empty state immediately.

```typescript
// /src/features/[feature]/components/ProductList.tsx
import { useProducts } from '../hooks/useProducts'
import ProductListSkeleton from './ProductListSkeleton'
import EmptyState from '@/shared/components/EmptyState'

export function ProductList() {
  const { products, loading, error } = useProducts()

  if (loading) return <ProductListSkeleton /> // Loading skeleton
  if (error) return <EmptyState message={`Error: ${error}`} />
  if (!products.length) return <EmptyState message="No products found" />

  return (
    <div className="grid gap-4">
      {products.map(product => (
        <ProductCard key={product.id} product={product} />
      ))}
    </div>
  )
}
```

**Checklist:**
- [ ] Loading state shows skeleton or spinner
- [ ] Error state shows error message (not just blank)
- [ ] Empty state shows friendly message (not blank)
- [ ] Spinner styled with Tailwind
- [ ] Skeleton loader created (matches content size/shape)
- [ ] No data displayed while loading (prevents layout shift)

**Why:** Better UX, prevents confused users from wondering "is it working?"

---

### 8. **Error Boundary / Error Display** ✅
Catch component errors and show them instead of white screen.

```typescript
// /src/features/[feature]/components/ProductList.tsx
export function ProductList() {
  const { products, loading, error } = useProducts()

  if (error) {
    return (
      <div className="p-4 bg-red-50 border border-red-200 rounded">
        <h3 className="font-semibold text-red-900">Unable to load products</h3>
        <p className="text-sm text-red-700">{error}</p>
        <button
          onClick={() => refetch()}
          className="mt-2 text-red-600 hover:text-red-700 underline"
        >
          Try again
        </button>
      </div>
    )
  }

  // ... rest of component
}
```

**Checklist:**
- [ ] Error state checked (if error, show error UI)
- [ ] Error message displayed to user
- [ ] "Try again" / refetch button available
- [ ] Error logged to console (Axios already does this)
- [ ] Styled visibly (red background with Tailwind)
- [ ] No error prevents entire page from loading (isolated to feature)

**Why:** Users know what went wrong, can retry instead of page being broken.

---

### 9. **Form Validation with React Hook Form** ✅
Form submission validates before sending to backend.

```typescript
// /src/features/[feature]/components/ProductFilter.tsx
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { ProductFilterSchema } from '@/shared/validation/product.schema'

export function ProductFilter() {
  const { register, handleSubmit, formState: { errors } } = useForm({
    resolver: zodResolver(ProductFilterSchema),
    defaultValues: { search: '', category: '', sortBy: 'name' }
  })

  const onSubmit = (data) => {
    console.log('Form valid, submitting:', data)
    // Call service with validated data
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
      <input
        {...register('search')}
        placeholder="Search products"
        className="w-full px-3 py-2 border border-gray-300 rounded"
      />
      {errors.search && <p className="text-red-500">{errors.search.message}</p>}

      <select {...register('category')} className="w-full px-3 py-2 border border-gray-300 rounded">
        <option value="">All Categories</option>
        <option value="electronics">Electronics</option>
        <option value="clothing">Clothing</option>
      </select>

      <button type="submit" className="px-4 py-2 bg-blue-600 text-white rounded">
        Filter
      </button>
    </form>
  )
}
```

**Checklist:**
- [ ] useForm initialized with zodResolver
- [ ] Zod schema passed to resolver
- [ ] register() called on inputs
- [ ] Error messages displayed below invalid fields
- [ ] Form doesn't submit if validation fails
- [ ] Form data typed (TypeScript knows shape)
- [ ] Submit handler called only when valid

**Why:** Prevents bad data from reaching backend, better UX with inline errors.

---

### 10. **Type Safety Throughout** ✅
Every function parameter and return type is typed, no `any`.

```typescript
// ❌ BAD - using any
function processProduct(product: any) {
  return product.name // Could be undefined!
}

// ✅ GOOD - properly typed
interface Product { id: string; name: string }
function processProduct(product: Product): string {
  return product.name // TypeScript knows product has name
}
```

**Checklist:**
- [ ] No `any` types (ESLint should warn)
- [ ] All function parameters typed
- [ ] All function return types specified
- [ ] Component props have interface (type Props { })
- [ ] API responses typed (Promise<Type>)
- [ ] Form data typed (z.infer<typeof schema>)
- [ ] No red squiggles in VS Code (TypeScript errors)

**Why:** TypeScript catches undefined errors at edit-time, not at runtime.

---

### 11. **Component Composition (Small, Focused)** ✅
Components do ONE thing well, not everything in one blob.

```typescript
// ❌ BAD - too much in one component (900 lines)
export function ProductPage() {
  // handles listing, filtering, sorting, cart, payment...
  return ...
}

// ✅ GOOD - split into focused components
export function ProductPage() {
  return (
    <div className="flex gap-4">
      <ProductFilter onFilter={...} />        {/* Just filtering UI */}
      <ProductList products={...} />          {/* Just list display */}
      <ProductDetails product={...} />        {/* Just detail display */}
    </div>
  )
}
```

**Checklist:**
- [ ] Component file < 200 lines (consider splitting if larger)
- [ ] Component has 1 primary responsibility
- [ ] Props defined in interface (type Props { })
- [ ] Complex logic extracted to hooks
- [ ] Complex JSX extracted to sub-components
- [ ] No prop drilling (use store for shared state)
- [ ] Reusable components in /shared/components

**Why:** Easier to test, debug, and reuse. Changes in one component don't affect others.

---

### 12. **Browser DevTools Console Monitoring** ✅
Every feature should be debuggable via console logs (no black box).

```typescript
// All API calls should show:
// 📤 API Request: GET /products { params }
// ✅ API Success: 200 /products { response data }
// ❌ API Error: 404 /products { error message }

// Add manual logs for complex flows:
console.log('⚙️ ProductFilter submitted:', { search: 'phone', category: 'electronics' })
console.log('📊 ProductList rendering:', products.length, 'items')
```

**Checklist:**
- [ ] No silent failures (all errors logged)
- [ ] Axios logs show (📤✅❌ visible in Console)
- [ ] Complex flows have manual console.log statements
- [ ] Error messages are readable (not dumped object)
- [ ] Network tab shows all API requests (no 404s, 500s)
- [ ] Can trace data flow through console
- [ ] DevTools Console can be read by non-engineers (clear messages)

**Why:** If something breaks, "check the console" immediately shows what happened.

---

### 13. **Component Props Documentation** ✅
Props are self-documenting so other developers know how to use component.

```typescript
interface ProductCardProps {
  /** Product data to display */
  product: Product
  /** Callback when product is clicked */
  onClick?: (productId: string) => void
  /** Show add-to-cart button */
  showCart?: boolean
  /** Disable click interactions */
  disabled?: boolean
}

export function ProductCard({ product, onClick, showCart = true, disabled = false }: ProductCardProps) {
  return (
    <div
      onClick={() => !disabled && onClick?.(product.id)}
      className={`cursor-pointer ${disabled ? 'opacity-50' : ''}`}
    >
      <img src={product.image} alt={product.name} />
      <h3>{product.name}</h3>
      <p>${product.price}</p>
      {showCart && !disabled && <button>Add to Cart</button>}
    </div>
  )
}
```

**Checklist:**
- [ ] Props interface created (type ComponentNameProps { })
- [ ] All props documented with JSDoc comments
- [ ] Optional props marked with `?`
- [ ] Default values provided (showCart = true)
- [ ] Props used consistently throughout component
- [ ] No required prop is missing in type definition
- [ ] Props are minimal and focused (not "pass entire store")

**Why:** Other developers (and future you) can use the component without reading source code.

---

### 14. **Accessibility (a11y) Basics** ✅
Ensure keyboard navigation and screen readers work.

```typescript
export function ProductFilter() {
  return (
    <form aria-label="Product filter">
      <label htmlFor="search">Search products</label>
      <input
        id="search"
        type="text"
        placeholder="Type product name..."
        aria-describedby="search-help"
      />
      <p id="search-help" className="text-sm text-gray-600">
        Enter at least 2 characters to search
      </p>

      <button type="submit" aria-label="Apply product filters">
        Filter
      </button>
    </form>
  )
}
```

**Checklist:**
- [ ] `<label>` connected to input with `htmlFor`
- [ ] Form has `aria-label` or legend
- [ ] Buttons have accessible text (not empty or just icon)
- [ ] Images have `alt` text
- [ ] Color not only way to show state (add text/icon)
- [ ] Tab order is logical (tab through form naturally)
- [ ] Error messages linked with `aria-describedby`

**Why:** Accessibility isn't optional - 1 in 4 people need it, plus better for SEO.

---

### 15. **Environment Variables & Config** ✅
API URLs, feature flags, and secrets in `.env`, not hardcoded.

```typescript
// .env
VITE_API_URL=http://localhost:8080/api/v1
VITE_FEATURE_ADMIN_ENABLED=true
VITE_FEATURE_ANALYTICS_ENABLED=true

// .env.production
VITE_API_URL=https://api.myecommerce.com/api/v1
VITE_FEATURE_ADMIN_ENABLED=true
```

```typescript
// src/shared/api/axios.ts
const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api/v1'

// src/features/admin/hooks/useAdminDashboard.ts
const ADMIN_ENABLED = import.meta.env.VITE_FEATURE_ADMIN_ENABLED === 'true'
if (!ADMIN_ENABLED) {
  throw new Error('Admin feature is disabled')
}
```

**Checklist:**
- [ ] API URL in `.env` (not hardcoded in service)
- [ ] Feature flags for incomplete features
- [ ] Secrets never in code (API keys in .env only)
- [ ] `.env` file added to `.gitignore` (don't commit secrets)
- [ ] `.env.example` created for reference (no secrets)
- [ ] Different `.env` files for dev/prod if needed
- [ ] Environment variables read via import.meta.env

**Why:** Can deploy same code to multiple environments (dev/staging/prod) without rebuilding.

---

## Implementation Workflow

### Before starting a feature:
1. **Print this checklist** and pin near your desk
2. **Read all 15 items** (2 min)
3. **Answer: "Which of these apply to my feature?"**
4. **Create folder structure** in /src/features/[feature]/

### While implementing:
1. **Define types first** (Concern #1)
2. **Build service** (Concern #2)
3. **Test service** (Concern #3)
4. **Build hook** (Concern #6)
5. **Build components** (Concern #11)
6. **Add validation** (Concern #4 & #9)
7. **Add error handling** (Concern #8)
8. **Add loading state** (Concern #7)
9. **Monitor console** (Concern #12)
10. **Check accessibility** (Concern #14)

### After completing a feature:
- [ ] Run `npm run lint` (no warnings)
- [ ] Run `npm run type-check` (no TypeScript errors)
- [ ] Run `npm run test` (all tests pass)
- [ ] Open DevTools Console (no errors)
- [ ] Check all endpoints in Network tab
- [ ] Verify Axios logs (📤✅❌ visible)
- [ ] Test on slow network (Throttle in DevTools)
- [ ] Test with keyboard only (no mouse)

---

## Quick Reference: Per-Feature Folders

```
/src/features/[feature]/
├── api/
│   ├── [feature]Service.ts          ← Concern #2
│   └── [feature]Service.test.ts     ← Concern #3
├── types/
│   └── index.ts                     ← Concern #1
├── store/
│   └── index.ts                     ← Concern #5
├── hooks/
│   ├── use[Feature].ts              ← Concern #6
│   └── use[Feature].test.ts
├── components/
│   ├── [Feature]List.tsx            ← Concern #11
│   ├── [Feature]Card.tsx            ← Concern #13
│   ├── [Feature]Filter.tsx          ← Concern #9
│   └── [Feature]Detail.tsx
├── guards/                          ← Only for auth/admin
│   └── ProtectedRoute.tsx
└── validation/                      ← Only if form inputs
    └── [feature].schema.ts          ← Concern #4
```

---

## Success Criteria

✅ When a feature passes ALL 15 concerns:
- Code won't have runtime errors (types, validation, error handling)
- Anyone can understand it (components, props, error messages)
- Bugs are visible in console (monitoring, logging)
- It works on production (config, accessibility, performance)
- It can be maintained (composition, types, documentation)

---

**Remember:** The best time to check this list was BEFORE you coded. The second-best time is RIGHT NOW.
