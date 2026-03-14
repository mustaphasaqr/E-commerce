import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '@/features/auth/hooks/useAuth'
import { Heart, ShoppingCart, Star } from 'lucide-react'
import { Button } from '@/shared/components/ui'

interface FeaturedProductsSectionProps {}

/**
 * Featured Products Section (Preline UI Style)
 *
 * Displays:
 * - Grid of featured/popular products
 * - Product card with image, rating, price
 * - "Add to Cart" button → requires auth
 * - "Wishlist" button → requires auth
 * - Shows placeholder products until backend API is integrated
 */
export function FeaturedProductsSection({}: FeaturedProductsSectionProps) {
  const navigate = useNavigate()
  const { isAuthenticated } = useAuth()
  const [wishlistItems, setWishlistItems] = useState<string[]>([])

  // Placeholder featured products
  const products = [
    {
      id: '1',
      name: 'Premium Wireless Headphones',
      price: 149.99,
      originalPrice: 199.99,
      rating: 4.8,
      reviews: 324,
      image: '🎧',
      badge: 'Best Seller',
    },
    {
      id: '2',
      name: 'Smart Watch Pro',
      price: 299.99,
      originalPrice: 399.99,
      rating: 4.7,
      reviews: 156,
      image: '⌚',
      badge: 'New',
    },
    {
      id: '3',
      name: 'Portable Power Bank',
      price: 49.99,
      originalPrice: 79.99,
      rating: 4.9,
      reviews: 512,
      image: '🔋',
      badge: 'Hot',
    },
    {
      id: '4',
      name: 'USB-C Cable 2m',
      price: 19.99,
      originalPrice: 29.99,
      rating: 4.6,
      reviews: 1024,
      image: '🔌',
      badge: null,
    },
    {
      id: '5',
      name: 'Wireless Charger',
      price: 79.99,
      originalPrice: 99.99,
      rating: 4.8,
      reviews: 432,
      image: '⚡',
      badge: 'Sale',
    },
    {
      id: '6',
      name: 'Phone Screen Protector',
      price: 9.99,
      originalPrice: 14.99,
      rating: 4.5,
      reviews: 2156,
      image: '📱',
      badge: null,
    },
  ]

  const handleAddToCart = (productId: string) => {
    if (!isAuthenticated) {
      navigate('/login?redirect=add-to-cart')
      return
    }
    // Action when authenticated - actual cart add will be implemented later
    console.log('Adding product to cart:', productId)
  }

  const toggleWishlist = (productId: string) => {
    if (!isAuthenticated) {
      navigate('/login?redirect=wishlist')
      return
    }
    // Toggle wishlist
    setWishlistItems((prev) =>
      prev.includes(productId) ? prev.filter((id) => id !== productId) : [...prev, productId]
    )
  }

  return (
    <section className="py-20 bg-white">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Section Header */}
        <div className="text-center space-y-4 mb-16">
          <h2 className="text-4xl font-bold text-gray-900">Featured Products</h2>
          <p className="text-xl text-gray-600">
            Discover our most popular items handpicked for you
          </p>
        </div>

        {/* Products Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
          {products.map((product) => (
            <div
              key={product.id}
              className="group bg-white border border-gray-200 rounded-xl shadow-sm hover:shadow-xl transition overflow-hidden"
            >
              {/* Product Image */}
              <div className="relative bg-gradient-to-br from-gray-100 to-gray-200 aspect-square flex items-center justify-center overflow-hidden">
                <span className="text-7xl group-hover:scale-110 transition duration-300">
                  {product.image}
                </span>

                {/* Badge */}
                {product.badge && (
                  <div className="absolute top-4 left-4 bg-red-500 text-white px-3 py-1 rounded-full text-sm font-semibold">
                    {product.badge}
                  </div>
                )}

                {/* Wishlist Button */}
                <button
                  onClick={() => toggleWishlist(product.id)}
                  className="absolute top-4 right-4 bg-white/80 hover:bg-white p-2 rounded-full shadow-md transition"
                  aria-label="Add to wishlist"
                >
                  <Heart
                    size={20}
                    className={
                      wishlistItems.includes(product.id) ? 'fill-red-500 text-red-500' : 'text-gray-600'
                    }
                  />
                </button>

                {/* Discount Badge */}
                {product.price < product.originalPrice && (
                  <div className="absolute bottom-4 right-4 bg-green-500 text-white px-3 py-1 rounded-full text-sm font-semibold">
                    -{Math.round(((product.originalPrice - product.price) / product.originalPrice) * 100)}%
                  </div>
                )}
              </div>

              {/* Product Info */}
              <div className="p-6 space-y-4">
                <div onClick={() => navigate(`/products/${product.id}`)} className="cursor-pointer">
                  <h3 className="text-lg font-semibold text-gray-900 group-hover:text-blue-600 transition">
                    {product.name}
                  </h3>
                </div>

                {/* Rating */}
                <div className="flex items-center gap-2">
                  <div className="flex items-center gap-1">
                    {[...Array(5)].map((_, i) => (
                      <Star
                        key={i}
                        size={16}
                        className={i < Math.floor(product.rating) ? 'fill-yellow-400 text-yellow-400' : 'text-gray-300'}
                      />
                    ))}
                  </div>
                  <span className="text-sm text-gray-600">{product.rating}</span>
                  <span className="text-sm text-gray-500">({product.reviews} reviews)</span>
                </div>

                {/* Price */}
                <div className="flex items-center gap-2 pt-2">
                  <span className="text-2xl font-bold text-gray-900">${product.price}</span>
                  {product.price < product.originalPrice && (
                    <span className="text-lg text-gray-400 line-through">${product.originalPrice}</span>
                  )}
                </div>

                {/* Add to Cart Button */}
                <Button
                  onClick={() => handleAddToCart(product.id)}
                  className="w-full bg-gradient-to-r from-blue-600 to-indigo-600 text-white py-3 rounded-lg font-semibold hover:shadow-lg transition flex items-center justify-center gap-2 group/btn"
                >
                  <ShoppingCart size={20} />
                  <span>Add to Cart</span>
                </Button>
              </div>
            </div>
          ))}
        </div>

        {/* View All Products CTA */}
        <div className="text-center mt-16">
          <Button
            onClick={() => navigate('/products')}
            className="bg-gray-900 text-white px-8 py-3 rounded-lg font-semibold hover:bg-gray-800 transition"
          >
            View All Products
          </Button>
        </div>
      </div>
    </section>
  )
}
