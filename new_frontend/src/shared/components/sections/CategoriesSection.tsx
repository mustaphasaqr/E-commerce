import { useNavigate } from 'react-router-dom'
import { ChevronRight } from 'lucide-react'

/**
 * Categories Section (Preline UI Style)
 *
 * Displays:
 * - Grid of product categories
 * - Each category is clickable and navigates to filtered products
 * - Emoji icons for visual appeal
 */
export function CategoriesSection() {
  const navigate = useNavigate()

  const categories = [
    { id: '1', name: 'Electronics', icon: '📱', description: 'Phones, gadgets & accessories' },
    { id: '2', name: 'Fashion', icon: '👕', description: 'Clothing & apparel' },
    { id: '3', name: 'Home & Garden', icon: '🏠', description: 'Furniture & décor' },
    { id: '4', name: 'Sports & Outdoors', icon: '⚽', description: 'Sports equipment' },
    { id: '5', name: 'Beauty & Health', icon: '💄', description: 'Cosmetics & wellness' },
    { id: '6', name: 'Books & Media', icon: '📚', description: 'Books & entertainment' },
  ]

  return (
    <section className="py-20 bg-gray-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Section Header */}
        <div className="text-center space-y-4 mb-16">
          <h2 className="text-4xl font-bold text-gray-900">Shop by Category</h2>
          <p className="text-xl text-gray-600">Find exactly what you're looking for</p>
        </div>

        {/* Categories Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {categories.map((category) => (
            <div
              key={category.id}
              onClick={() => navigate(`/products?category=${category.id}`)}
              className="group cursor-pointer bg-white rounded-xl shadow-md hover:shadow-xl transition p-8 space-y-4"
            >
              <div className="text-6xl group-hover:scale-110 transition duration-300">{category.icon}</div>
              <div>
                <h3 className="text-2xl font-bold text-gray-900 group-hover:text-blue-600 transition flex items-center justify-between">
                  {category.name}
                  <ChevronRight
                    size={24}
                    className="opacity-0 group-hover:opacity-100 transition transform group-hover:translate-x-1"
                  />
                </h3>
                <p className="text-gray-600 mt-2">{category.description}</p>
              </div>
            </div>
          ))}
        </div>
      </div>
    </section>
  )
}
