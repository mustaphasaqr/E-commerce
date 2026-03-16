import { useEffect, useMemo, useState } from 'react'
import { ArrowRight, Filter, Search, ShoppingCart, Sparkles } from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import { Button, Card, CardContent, CardDescription, CardHeader, CardTitle, Input } from '@/shared/components/ui'
import productService from '@/features/products/api/productService'
import cartService from '@/features/cart/api/cartService'
import type { ProductListItem } from '@/features/products/types'

const PAGE_SIZE = 12

type SortOption = 'name-asc' | 'name-desc' | 'price-asc' | 'price-desc' | 'stock-desc'

const formatMoney = (value: number, currency = 'USD'): string =>
  new Intl.NumberFormat('en-US', { style: 'currency', currency }).format(value ?? 0)

const classifyCategory = (product: ProductListItem): string => {
  const source = `${product.name} ${product.sku}`.toLowerCase()
  if (source.includes('phone') || source.includes('laptop') || source.includes('headphone')) return 'Electronics'
  if (source.includes('shirt') || source.includes('dress') || source.includes('shoe')) return 'Fashion'
  if (source.includes('cream') || source.includes('makeup') || source.includes('skin')) return 'Beauty'
  if (source.includes('chair') || source.includes('table') || source.includes('home')) return 'Home'
  return 'General'
}

export default function ProductsPage() {
  const navigate = useNavigate()
  const [products, setProducts] = useState<ProductListItem[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [search, setSearch] = useState('')
  const [selectedCategory, setSelectedCategory] = useState('All')
  const [sort, setSort] = useState<SortOption>('name-asc')
  const [page, setPage] = useState(1)
  const [busyProductId, setBusyProductId] = useState<string | null>(null)

  useEffect(() => {
    const load = async () => {
      setIsLoading(true)
      setError(null)
      try {
        const list = await productService.listProducts()
        setProducts(list.filter((item) => item.active))
      } catch {
        setError('Failed to load products. Please try again.')
        setProducts([])
      } finally {
        setIsLoading(false)
      }
    }

    void load()
  }, [])

  const categories = useMemo(() => {
    const unique = new Set<string>(products.map((item) => classifyCategory(item)))
    return ['All', ...Array.from(unique).sort((a, b) => a.localeCompare(b))]
  }, [products])

  const filtered = useMemo(() => {
    const term = search.trim().toLowerCase()
    const list = products.filter((item) => {
      const inCategory = selectedCategory === 'All' || classifyCategory(item) === selectedCategory
      if (!inCategory) return false
      if (!term) return true
      return (
        item.name.toLowerCase().includes(term) ||
        item.sku.toLowerCase().includes(term)
      )
    })

    return [...list].sort((a, b) => {
      switch (sort) {
        case 'name-desc':
          return b.name.localeCompare(a.name)
        case 'price-asc':
          return a.price - b.price
        case 'price-desc':
          return b.price - a.price
        case 'stock-desc':
          return b.availableStock - a.availableStock
        default:
          return a.name.localeCompare(b.name)
      }
    })
  }, [products, search, selectedCategory, sort])

  const totalPages = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE))
  const currentPage = Math.min(page, totalPages)
  const paged = filtered.slice((currentPage - 1) * PAGE_SIZE, currentPage * PAGE_SIZE)

  useEffect(() => {
    if (currentPage !== page) {
      setPage(currentPage)
    }
  }, [currentPage, page])

  const addToCart = async (product: ProductListItem) => {
    setBusyProductId(product.id)
    try {
      await cartService.addToCart({ productId: product.id, quantity: 1 })
      navigate('/cart')
    } catch {
      setError('Could not add item to cart. Please retry.')
    } finally {
      setBusyProductId(null)
    }
  }

  return (
    <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
      <section className="relative overflow-hidden rounded-2xl border border-amber-200 bg-gradient-to-r from-amber-50 via-white to-sky-50 p-6 sm:p-8">
        <div className="absolute -right-16 -top-16 h-48 w-48 rounded-full bg-amber-200/30 blur-2xl" />
        <div className="absolute -left-12 -bottom-12 h-44 w-44 rounded-full bg-sky-200/40 blur-2xl" />
        <div className="relative space-y-3">
          <p className="inline-flex items-center gap-2 rounded-full bg-white/80 px-3 py-1 text-xs font-semibold uppercase tracking-wide text-amber-700">
            <Sparkles className="h-3.5 w-3.5" /> Curated marketplace
          </p>
          <h1 className="text-3xl font-bold tracking-tight text-gray-900 sm:text-4xl">Find your next favorite product</h1>
          <p className="max-w-2xl text-sm text-gray-600 sm:text-base">
            Explore the catalog with smart filters and a fast add-to-cart flow built for real shopping journeys.
          </p>
        </div>
      </section>

      <Card className="mt-6 border-gray-200/80">
        <CardHeader className="pb-4">
          <CardTitle className="flex items-center gap-2 text-xl">
            <Filter className="h-5 w-5 text-sky-700" /> Filter & Sort
          </CardTitle>
          <CardDescription>Search by name or SKU, then refine by category and sorting.</CardDescription>
        </CardHeader>
        <CardContent className="grid gap-3 md:grid-cols-4">
          <Input
            value={search}
            onChange={(event) => {
              setSearch(event.target.value)
              setPage(1)
            }}
            placeholder="Search products..."
            icon={<Search className="h-4 w-4" />}
          />

          <select
            value={selectedCategory}
            onChange={(event) => {
              setSelectedCategory(event.target.value)
              setPage(1)
            }}
            className="h-10 rounded-md border border-gray-300 bg-white px-3 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            {categories.map((category) => (
              <option key={category} value={category}>{category}</option>
            ))}
          </select>

          <select
            value={sort}
            onChange={(event) => {
              setSort(event.target.value as SortOption)
              setPage(1)
            }}
            className="h-10 rounded-md border border-gray-300 bg-white px-3 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            <option value="name-asc">Name (A-Z)</option>
            <option value="name-desc">Name (Z-A)</option>
            <option value="price-asc">Price (Low to High)</option>
            <option value="price-desc">Price (High to Low)</option>
            <option value="stock-desc">Stock (High to Low)</option>
          </select>

          <div className="flex items-center rounded-md border border-dashed border-gray-300 px-3 text-sm text-gray-600">
            {filtered.length} result{filtered.length === 1 ? '' : 's'}
          </div>
        </CardContent>
      </Card>

      {error && (
        <div className="mt-4 rounded-md border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
          {error}
        </div>
      )}

      {isLoading ? (
        <div className="mt-8 text-sm text-gray-600">Loading products...</div>
      ) : (
        <>
          <div className="mt-6 grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
            {paged.map((product) => (
              <Card key={product.id} className="group border-gray-200 transition hover:-translate-y-0.5 hover:shadow-md">
                <CardHeader className="space-y-3">
                  <div className="flex items-start justify-between gap-2">
                    <div>
                      <CardTitle className="text-lg leading-tight">{product.name}</CardTitle>
                      <CardDescription>SKU: {product.sku}</CardDescription>
                    </div>
                    <span className="rounded-full bg-amber-100 px-2 py-1 text-xs font-semibold text-amber-700">
                      {classifyCategory(product)}
                    </span>
                  </div>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="flex items-center justify-between">
                    <span className="text-xl font-bold text-gray-900">{formatMoney(product.price, product.currency)}</span>
                    <span className="text-xs text-gray-500">Stock: {product.availableStock}</span>
                  </div>

                  <div className="grid grid-cols-2 gap-2">
                    <Button
                      variant="outline"
                      onClick={() => navigate(`/products/${product.id}`)}
                    >
                      Details <ArrowRight className="ml-1 h-3.5 w-3.5" />
                    </Button>
                    <Button
                      onClick={() => void addToCart(product)}
                      disabled={busyProductId === product.id || product.availableStock <= 0}
                    >
                      <ShoppingCart className="mr-1 h-4 w-4" />
                      {product.availableStock <= 0 ? 'Out of Stock' : 'Add to Cart'}
                    </Button>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>

          <div className="mt-6 flex items-center justify-between gap-3 rounded-xl border border-gray-200 bg-white px-4 py-3">
            <Button
              variant="outline"
              onClick={() => setPage((prev) => Math.max(1, prev - 1))}
              disabled={currentPage <= 1}
            >
              Previous
            </Button>
            <span className="text-sm text-gray-600">Page {currentPage} of {totalPages}</span>
            <Button
              variant="outline"
              onClick={() => setPage((prev) => Math.min(totalPages, prev + 1))}
              disabled={currentPage >= totalPages}
            >
              Next
            </Button>
          </div>
        </>
      )}
    </div>
  )
}
