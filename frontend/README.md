# E-Commerce Frontend

> **React 18 + TypeScript + Redux Toolkit + Vite**
>
> Modern, scalable e-commerce frontend application following domain-driven architecture and SOLID principles.

## 🎯 Architecture

The frontend is organized by **business domains** (matching the backend), not technical layers:

```
src/
├── shared/             # Reusable utilities, components, hooks
├── auth/               # Authentication module
├── user/               # User profile & settings
├── product/            # Product catalog & browsing
├── cart/               # Shopping cart
├── order/              # Checkout & order management
├── admin/              # Admin dashboard & analytics
├── observability/      # Client-side monitoring (Sentry, GA)
├── layout/             # Navigation & page layout
└── store/              # Redux global state
```

## 🚀 Quick Start

### Installation

```bash
# Install dependencies
npm install

# Create env file
cp .env.example .env.local
# Edit .env.local with your configuration
```

### Development

```bash
# Start dev server (http://localhost:3000)
npm run dev

# Type checking
npm run type-check

# Linting
npm run lint
npm run lint:fix
```

### Testing

```bash
# Run tests
npm test

# Run tests with UI
npm run test:ui

# Coverage report
npm run test:coverage
```

### Production Build

```bash
# Build for production
npm run build

# Preview production build locally
npm run preview
```

## 📁 Module Structure

Each module follows this pattern:

```
module/
├── components/        # UI components
├── pages/            # Page-level components
├── hooks/            # Custom hooks with business logic
├── services/         # API calls to backend
├── store/            # Redux slices & selectors
├── types/            # TypeScript definitions
├── utils/            # Helper functions
└── index.ts          # Public exports
```

### Example: Product Module

- **Components**: `ProductCard`, `ProductList`, `ProductDetail`, `ProductFilters`
- **Pages**: `ProductsPage`, `ProductDetailPage`, `SearchResultsPage`
- **Hooks**: `useProducts()`, `useProduct()`, `useProductFilter()`, `useProductReviews()`
- **Services**: Calls `/api/v1/products/*` endpoints
- **Store**: Redux slices for product state, filter state, selection
- **Types**: `Product`, `ProductFilter`, `ProductReview`, etc.

## 🔌 API Integration

Frontend communicates with 7 backend bounded contexts:

| Module | Backend API |
|--------|------------|
| `auth/` | `/api/v1/auth/*` |
| `user/` | `/api/v1/users/*` |
| `product/` | `/api/v1/products/*` |
| `cart/` | `/api/v1/cart/*` |
| `order/` | `/api/v1/orders/*` |
| `admin/` | `/api/v1/admin/*`, `/api/v1/analytics/*` |

## 🏗️ Architectural Principles

### 1. **SOLID Principles**

- **S**ingle Responsibility: Each component/hook has one reason to change
- **O**pen/Closed: Extend functionality without modifying existing code
- **L**iskov Substitution: Components are substitutable
- **I**nterface Segregation: Fine-grained interfaces
- **D**ependency Inversion: Depend on abstractions, not concrete implementations

### 2. **Domain-Driven Design**

Organized by business domains (user, product, order) not technical layers (components, utilities). This matches the backend architecture and makes scaling easier.

### 3. **Separation of Concerns**

- **Presentational Components**: Dumb, reusable, receive props
- **Container Components**: Smart, connect to state, manage logic
- **Custom Hooks**: Extract and reuse business logic
- **Services**: Handle API communication
- **Store**: Centralized state management (Redux)

### 4. **Performance First**

- Code splitting by route
- Lazy loading of components
- Image optimization
- Caching strategies
- Virtual scrolling for large lists

### 5. **Security First**

- XSS prevention (React auto-escapes)
- CSRF token handling
- Secure token storage (HttpOnly cookies)
- Input validation & sanitization
- CSP headers compliance

## 📊 State Management

Uses **Redux Toolkit** with async thunks for API calls:

```typescript
// store/slices/productSlice.ts
export const fetchProducts = createAsyncThunk(
  'products/fetchProducts',
  async (filters: Filters) => {
    return await productService.getAllProducts(filters);
  }
);

// Usage in component
const { products, loading } = useSelector(state => state.products);
const dispatch = useDispatch();
useEffect(() => {
  dispatch(fetchProducts(filters));
}, [dispatch, filters]);
```

## 🧪 Testing Strategy

**Test Pyramid**: 60% unit, 35% integration, 5% E2E

- **Unit Tests**: Individual functions, components (Jest + React Testing Library)
- **Integration Tests**: Component interactions with Redux
- **E2E Tests**: Full user journeys (Cypress - separate config)

```bash
# Run unit & integration tests
npm test

# E2E tests require separate setup
# cypress open
```

## 🛡️ Security Features

- JWT token management with refresh logic
- Protected routes (auth guards)
- XSS prevention with DOMPurify
- CSRF token handling
- Secure password handling
- Rate limiting on client side
- Error boundary for graceful error handling

## 📡 API Interceptors

All API requests go through centralized interceptor:

```typescript
// shared/middleware/api-interceptor.ts
- Adds JWT token to headers
- Handles 401 unauthorized (refresh token)
- Retries failed requests (configurable)
- Transforms error responses
- Logs requests (dev only)
```

## 🔍 Observability

### Error Tracking

**Sentry** integration:
- Captures JavaScript errors
- Session replay for debugging
- Error frequency & patterns
- Sourcemap support

### Performance Monitoring

**Core Web Vitals**:
- LCP (Largest Contentful Paint) < 2.5s
- FID (First Input Delay) < 100ms
- CLS (Cumulative Layout Shift) < 0.1

### Analytics

**Google Analytics** + **Segment**:
- User behavior tracking
- Conversion funnels
- Custom events
- User properties

## 🚀 Deployment

### Docker

```dockerfile
# Multi-stage build
FROM node:20-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM node:20-alpine
RUN npm install -g serve
WORKDIR /app
COPY --from=builder /app/dist ./dist
EXPOSE 3000
CMD ["serve", "-s", "dist", "-l", "3000"]
```

### CI/CD

GitHub Actions pipeline:
1. Lint & type check
2. Unit test coverage (>80%)
3. Build verification
4. Deployment to Railway (on push to main)

## 📚 Best Practices

### Naming Conventions

- **Components**: PascalCase (e.g., `ProductCard.tsx`)
- **Hooks**: camelCase with `use` prefix (e.g., `useProduct.ts`)
- **Services**: camelCase with `.service` (e.g., `product.service.ts`)
- **Types**: PascalCase (e.g., `Product.ts`, `ProductFilter.ts`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `API_TIMEOUT`)

### Folder Organization

- Keep related files together (colocate)
- One component per file (unless very small)
- Index files for clean imports (`import { Component } from '@product'`)
- No circular dependencies

### Code Style

- Use TypeScript strict mode
- Prefer functional components with hooks
- Avoid prop drilling (use Redux or context)
- Keep components small and testable
- Write self-documenting code

## 🔗 Useful Links

- [React Documentation](https://react.dev)
- [Redux Toolkit Documentation](https://redux-toolkit.js.org)
- [React Router Documentation](https://reactrouter.com)
- [Vite Documentation](https://vitejs.dev)
- [TypeScript Handbook](https://www.typescriptlang.org/docs)

## 📝 License

© 2024 E-Commerce. All rights reserved.
