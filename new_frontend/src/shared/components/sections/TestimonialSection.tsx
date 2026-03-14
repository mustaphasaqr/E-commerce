import { Star } from 'lucide-react'

/**
 * Testimonial Section (Preline UI Style)
 *
 * Displays:
 * - Customer testimonials
 * - Star ratings
 * - Customer avatars and names
 * - Social proof to build trust
 */
export function TestimonialSection() {
  const testimonials = [
    {
      id: '1',
      name: 'Emma Johnson',
      role: 'Verified Buyer',
      avatar: '👩',
      rating: 5,
      text: 'Amazing selection and super fast delivery! The product quality exceeded my expectations. Will definitely shop here again.',
    },
    {
      id: '2',
      name: 'Michael Chen',
      role: 'Verified Buyer',
      avatar: '👨',
      rating: 5,
      text: 'Great customer service and very transparent about shipping costs. Best online shopping experience I\'ve had.',
    },
    {
      id: '3',
      name: 'Sarah Williams',
      role: 'Verified Buyer',
      avatar: '👩',
      rating: 4,
      text: 'Good variety of products and competitive pricing. Packaging was secure and product arrived in perfect condition.',
    },
  ]

  return (
    <section className="py-20 bg-white">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Section Header */}
        <div className="text-center space-y-4 mb-16">
          <h2 className="text-4xl font-bold text-gray-900">What Our Customers Say</h2>
          <p className="text-xl text-gray-600">Join thousands of happy shoppers</p>
        </div>

        {/* Testimonials Grid */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          {testimonials.map((testimonial) => (
            <div key={testimonial.id} className="bg-gray-50 rounded-xl p-8 space-y-4 border border-gray-200">
              {/* Rating Stars */}
              <div className="flex gap-1">
                {[...Array(5)].map((_, i) => (
                  <Star
                    key={i}
                    size={20}
                    className={
                      i < testimonial.rating ? 'fill-yellow-400 text-yellow-400' : 'text-gray-300'
                    }
                  />
                ))}
              </div>

              {/* Testimonial Text */}
              <p className="text-gray-700 italic">"{testimonial.text}"</p>

              {/* Customer Info */}
              <div className="flex items-center gap-3 pt-4 border-t border-gray-200">
                <span className="text-4xl">{testimonial.avatar}</span>
                <div>
                  <p className="font-semibold text-gray-900">{testimonial.name}</p>
                  <p className="text-sm text-gray-600">{testimonial.role}</p>
                </div>
              </div>
            </div>
          ))}
        </div>

        {/* Stats */}
        <div className="mt-20 grid grid-cols-1 md:grid-cols-3 gap-8 text-center">
          <div>
            <div className="text-4xl font-bold text-blue-600">50K+</div>
            <p className="text-gray-600 mt-2">Products Available</p>
          </div>
          <div>
            <div className="text-4xl font-bold text-indigo-600">100K+</div>
            <p className="text-gray-600 mt-2">Happy Customers</p>
          </div>
          <div>
            <div className="text-4xl font-bold text-purple-600">24/7</div>
            <p className="text-gray-600 mt-2">Customer Support</p>
          </div>
        </div>
      </div>
    </section>
  )
}
