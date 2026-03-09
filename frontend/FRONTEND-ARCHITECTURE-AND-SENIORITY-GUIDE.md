# 🎨 Frontend Architecture & Senior Engineer's Guide

> **دليل شامل للـ Frontend Architecture و Best Practices**  
> الـ Architecture التي تجعلك Senior/Architect + Security + Performance + Testing + Deployment

---

## 📋 Table of Contents

1. [Frontend Architecture Fundamentals](#1-frontend-architecture-fundamentals)
2. [Component-Driven Architecture](#2-component-driven-architecture)
3. [State Management Patterns](#3-state-management-patterns)
4. [SOLID Principles in Frontend](#4-solid-principles-in-frontend)
5. [Design Patterns for Frontend](#5-design-patterns-for-frontend)
6. [Frontend Security (الأمان)](#6-frontend-security)
7. [Responsive Design & Mobile-First (الاستجابة والموبايل)](#7-responsive-design--mobile-first-الاستجابة-والموبايل)
8. [Frontend Performance (الأداء)](#8-frontend-performance-الأداء)
9. [Frontend Testing Strategy](#9-frontend-testing-strategy)
10. [Observability & Analytics](#10-observability--analytics)
11. [Deployment & CI/CD for Frontend](#11-deployment--cicd-for-frontend)
12. [Resilience & Fault Tolerance](#12-resilience--fault-tolerance)
13. [Data Integrity & Synchronization](#13-data-integrity--synchronization)
14. [Implementation Status](#14-implementation-status)
15. [Senior Engineer Study Plan](#15-senior-engineer-study-plan)

---

## 1. Frontend Architecture Fundamentals

### 🏗️ **A. What is Frontend Architecture?**

**Definition:**  
Frontend architecture is the structure, patterns, and best practices that enable you to:
- Build scalable applications (1000+ components)
- Maintain code quality as team grows
- Enable fast onboarding for new developers
- Support long-term maintenance and refactoring
- Ensure performance, security, and reliability

**Difference from Backend:**

```
Backend Architecture:
- Layers: Controller → Service → Repository → Database
- Clear separation of concerns
- Well-defined patterns and standards

Frontend Architecture:
- Similar layering required!
- Component → Custom Hook/Logic → Store → API Layer
- Often overlooked, leads to "spaghetti code"
```

---

### 📁 **B. Frontend Project Structure (Current Implementation)**

```
E-commerce/                              ← Monorepo root
├── frontend/                            ← React Frontend (separate project)
│   ├── src/
│   │   ├── shared/                      # Shared across all modules
│   │   │   ├── components/              # Button, Modal, Spinner, Card, Forms
│   │   │   ├── hooks/                   # useApi, useLocalStorage, useForm, etc.
│   │   │   ├── utils/                   # formatters, validators, helpers
│   │   │   ├── types/                   # api.types, common.types, error.types
│   │   │   ├── styles/                  # globals.css, variables, theme
│   │   │   ├── config/                  # api.config, app.config, routes.config
│   │   │   ├── services/                # apiClient, storage, notification
│   │   │   ├── middleware/              # api-interceptor, error-handler
│   │   │   └── observability/           # CLIENT-SIDE technical monitoring
│   │   │       ├── services/            # error-tracking (Sentry), analytics (GA)
│   │   │       ├── hooks/               # usePageView, useErrorTracking
│   │   │       ├── events/              # product-events, order-events
│   │   │       └── types/               # observability.types
│   │   │
│   │   ├── auth/                        # Maps to /api/v1/auth/*
│   │   │   ├── components/              # LoginForm, RegisterForm, MFA
│   │   │   ├── pages/                   # LoginPage, RegisterPage
│   │   │   ├── hooks/                   # useAuth, useLogin, useRefreshToken
│   │   │   ├── services/                # auth.service, token.service
│   │   │   ├── store/                   # authSlice (Redux)
│   │   │   ├── types/                   # auth.types, login.types
│   │   │   ├── guards/                  # auth.guard, admin.guard
│   │   │   └── context/                 # AuthContext (alternative)
│   │   │
│   │   ├── user/                        # Maps to /api/v1/users/*
│   │   │   ├── components/              # UserProfile, Settings, AddressManager
│   │   │   ├── pages/                   # ProfilePage, SettingsPage
│   │   │   ├── hooks/                   # useUser, useProfile, usePreferences
│   │   │   ├── services/                # user.service, profile.service
│   │   │   ├── store/                   # userSlice, userSelectors
│   │   │   └── types/                   # user.types, address.types
│   │   │
│   │   ├── product/                     # Maps to /api/v1/products/*
│   │   │   ├── components/              # ProductCard, ProductList, ProductDetail, Filters
│   │   │   ├── pages/                   # ProductsPage, ProductDetailPage
│   │   │   ├── hooks/                   # useProducts, useProduct, useProductFilter
│   │   │   ├── services/                # product.service, search.service
│   │   │   ├── store/                   # productSlice, filterSlice
│   │   │   ├── types/                   # product.types, filter.types
│   │   │   └── utils/                   # filterHelpers, sortHelpers
│   │   │
│   │   ├── cart/                        # Maps to /api/v1/cart/*
│   │   │   ├── components/              # CartIcon, CartPage, CartItem
│   │   │   ├── pages/                   # CartPage
│   │   │   ├── hooks/                   # useCart, useAddToCart, usePromoCode
│   │   │   ├── services/                # cart.service, cart-sync.service
│   │   │   ├── store/                   # cartSlice, cartSelectors
│   │   │   ├── types/                   # cart.types, promo.types
│   │   │   └── utils/                   # cart-calculator
│   │   │
│   │   ├── order/                       # Maps to /api/v1/orders/*
│   │   │   ├── components/              # OrderForm, OrderPayment, OrderStatus
│   │   │   ├── pages/                   # CheckoutPage, OrderConfirmationPage
│   │   │   ├── hooks/                   # useOrder, useCheckout, usePayment
│   │   │   ├── services/                # order.service, checkout.service
│   │   │   ├── store/                   # orderSlice, checkoutSlice
│   │   │   └── types/                   # order.types, checkout.types
│   │   │
│   │   ├── admin/                       # Maps to /api/v1/admin/* + /api/v1/analytics/*
│   │   │   ├── components/              # AdminLayout, Sidebar, TopBar, StatsCard
│   │   │   ├── pages/                   # AdminDashboardPage (entry point)
│   │   │   │
│   │   │   ├── features/
│   │   │   │   ├── analytics/           # 🔴 BUSINESS ANALYTICS (/api/v1/analytics/*)
│   │   │   │   │   ├── components/      # RevenueChart, SalesMetrics, Filters
│   │   │   │   │   ├── pages/           # AnalyticsPage
│   │   │   │   │   ├── hooks/           # useDailySales, useBestSelling, useTopRevenue
│   │   │   │   │   ├── services/        # analytics.service (all /api/v1/analytics/*)
│   │   │   │   │   ├── store/           # analyticsSlice, analyticsSelectors
│   │   │   │   │   ├── types/           # analytics.types
│   │   │   │   │   └── utils/           # chart-formatters, analytics-helpers
│   │   │   │   │
│   │   │   │   ├── products/            # Product management (/api/v1/admin/products/*)
│   │   │   │   │   ├── components/
│   │   │   │   │   ├── pages/
│   │   │   │   │   ├── hooks/
│   │   │   │   │   ├── services/
│   │   │   │   │   └── store/
│   │   │   │   │
│   │   │   │   ├── orders/              # Order management (/api/v1/admin/orders/*)
│   │   │   │   │   ├── components/
│   │   │   │   │   ├── pages/
│   │   │   │   │   ├── hooks/
│   │   │   │   │   ├── services/
│   │   │   │   │   └── store/
│   │   │   │   │
│   │   │   │   └── users/               # User management (/api/v1/admin/users/*)
│   │   │   │       ├── components/
│   │   │   │       ├── pages/
│   │   │   │       ├── hooks/
│   │   │   │       ├── services/
│   │   │   │       └── store/
│   │   │   │
│   │   │   ├── hooks/                   # useAdminAuth (check OWNER/ADMIN role)
│   │   │   ├── services/                # admin.service (non-analytics)
│   │   │   ├── store/                   # adminSlice, adminSelectors
│   │   │   ├── types/                   # admin.types, dashboard.types
│   │   │   └── guards/                  # admin.guard (route protection)
│   │   │
│   │   ├── layout/                      # Navigation & page structure
│   │   │   ├── components/              # Header, Navigation, Footer
│   │   │   ├── pages/                   # NotFoundPage, ErrorPage
│   │   │   └── types/                   # navigation.types
│   │   │
│   │   ├── store/                       # Global Redux Store
│   │   │   ├── index.ts                 # Store configuration
│   │   │   ├── slices/                  # All Redux slices combined
│   │   │   └── hooks/                   # useAppDispatch, useAppSelector (typed)
│   │   │
│   │   ├── test/                        # Vitest configuration
│   │   │   └── setup.ts                 # Test environment setup
│   │   │
│   │   ├── App.tsx                      # Root React component (entry)
│   │   ├── main.tsx                     # React DOM render
│   │   ├── routes.tsx                   # Route definitions
│   │   └── env.d.ts                     # Vite environment types
│   │
│   ├── index.html                       # HTML entry point
│   ├── package.json                     # Dependencies
│   ├── tsconfig.json                    # TypeScript configuration
│   ├── tsconfig.node.json               # TypeScript (bundler)
│   ├── vite.config.ts                   # Vite configuration
│   ├── vitest.config.ts                 # Vitest configuration
│   ├── .env.example                     # Environment variables
│   ├── .gitignore                       # Git exclusions
│   └── README.md                        # Frontend documentation
│
├── pom.xml                              # Backend (Java/Spring Boot)
├── src/                                 # Backend source
├── docker-compose.yml                   # Full stack orchestration
└── ... (backend files)
```

**Why This Structure Works:**

✅ **10 Bounded Context Modules** - Maps directly to backend `@deprecated` contexts  
✅ **Consistent Organization** - Each module has identical structure (components, hooks, services, store, types)  
✅ **API Mapping Clear** - Each module knows which `/api/v1/*` endpoints it calls  
✅ **Separate Observability** - Client-side monitoring (Sentry, GA) separate from backend monitoring  
✅ **Admin Complexity Handled** - Analytics distinction (business vs technical) is clear  
✅ **Redux Scalable** - Global store at root, with per-module slices  
✅ **Monorepo Ready** - Frontend + Backend in one repo, coordinated deployments  

**Key Differences from Backend:**

| Aspect | Backend (Java) | Frontend (React) |
|--------|---|---|
| **Module Organization** | Bounded contexts with entities | Same! 10 modules mapping to API contexts |
| **Layers** | Controller → Service → Repository | Component → Custom Hook → Service → Store |
| **Testing** | src/test folder | Colocated .test.tsx files (modern practice) |
| **Configuration** | application.properties | .env.example + config/ folder |
| **Build Tool** | Maven (pom.xml) | Vite + npm |
| **State Management** | Database | Redux store (in-memory) |
| **Error Handling** | Global @ExceptionHandler | Middleware + Error Boundary |



**Module Organization Pattern (Like Your Backend):**

Each module follows a similar structure:
- `components/` → UI components
- `pages/` → Page-level components (routes)
- `hooks/` → Business logic encapsulation
- `services/` → API calls
- `store/` → State management (Redux slices, selectors)
- `types/` → TypeScript definitions
- `utils/` → Helper functions
- `guards/` → Route protection (auth, permissions)

---

### 🎯 **C. Architecture Layers (Like Backend)**

```
Frontend Architecture = Backend Architecture adapted for UI

┌─────────────────────────────────────────────────────────┐
│ Presentation Layer (UI Components)                      │
│ - React Components                                      │
│ - Page Components                                       │
│ - Layout Components                                     │
└──────────────────────┬──────────────────────────────────┘
                       ↓
┌─────────────────────────────────────────────────────────┐
│ Business Logic Layer (Custom Hooks & Services)          │
│ - Custom Hooks (useApi, useAuth)                        │
│ - Service Classes                                       │
│ - Validation Logic                                      │
│ - Formatting Logic                                      │
└──────────────────────┬──────────────────────────────────┘
                       ↓
┌─────────────────────────────────────────────────────────┐
│ State Management Layer (Redux/Zustand)                  │
│ - Stores                                                │
│ - Selectors                                             │
│ - Reducers                                              │
│ - Middleware                                            │
└──────────────────────┬──────────────────────────────────┘
                       ↓
┌─────────────────────────────────────────────────────────┐
│ API Layer (HTTP Client)                                 │
│ - Axios/Fetch Wrapper                                   │
│ - Request/Response Interceptors                         │
│ - Error Handling                                        │
│ - Request Caching                                       │
└──────────────────────┬──────────────────────────────────┘
                       ↓
┌─────────────────────────────────────────────────────────┐
│ Backend API (Your Spring Boot)                          │
└─────────────────────────────────────────────────────────┘
```

---

### 🚫 **D. Anti-Patterns (What NOT to Do)**

**Anti-Pattern 1: Everything in Components**

```jsx
// ❌ BAD: Business logic in component
const ProductDetail = () => {
  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  
  useEffect(() => {
    setLoading(true);
    fetch(`/api/products/${id}`)
      .then(res => res.json())
      .then(data => {
        setProduct(data);
        // Validation logic
        if (!data.name) throw new Error('Invalid product');
        // Formatting logic
        data.price = data.price.toFixed(2);
        // Cache logic
        localStorage.setItem(`product_${id}`, JSON.stringify(data));
      })
      .catch(err => setError(err))
      .finally(() => setLoading(false));
  }, [id]);
  
  return <div>{product?.name}</div>;
};

// Problem: Component does everything - hard to test, reuse, maintain
```

**✅ GOOD: Separation of Concerns**

```jsx
// Custom Hook handles API logic
const useProduct = (id) => {
  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  
  useEffect(() => {
    // Call service (which handles everything)
    productService.getProduct(id)
      .then(setProduct)
      .catch(setError)
      .finally(() => setLoading(false));
  }, [id]);
  
  return { product, loading, error };
};

// Component only handles UI
const ProductDetail = ({ id }) => {
  const { product, loading, error } = useProduct(id);
  
  if (loading) return <Spinner />;
  if (error) return <ErrorBoundary error={error} />;
  
  return <div>{product?.name}</div>;
};
```

---

## 2. Component-Driven Architecture

### 🧩 **A. Component Classification**

```
Components can be classified into 3 types:

1. PRESENTATIONAL (Dumb Components)
   - Only receive props
   - No business logic
   - No API calls
   - Reusable across features
   Example: Button, Card, Modal, Input

2. CONTAINER (Smart Components)
   - Connect to store (Redux/Zustand)
   - Contains business logic
   - Manages child components
   - Feature-specific
   Example: ProductListContainer, AuthContainer

3. LAYOUT Components
   - Define page structure
   - Combine multiple components
   - Handle navigation
   Example: MainLayout, AdminLayout
```

---

### 📝 **B. Component Best Practices**

**Pattern 1: Presentational Component**

```tsx
// File: components/common/ProductCard.tsx
interface ProductCardProps {
  id: string;
  name: string;
  price: number;
  image: string;
  onAddToCart: (productId: string) => void;
}

export const ProductCard: React.FC<ProductCardProps> = ({
  id,
  name,
  price,
  image,
  onAddToCart,
}) => {
  return (
    <div className="product-card">
      <img src={image} alt={name} />
      <h3>{name}</h3>
      <p className="price">${price.toFixed(2)}</p>
      <button onClick={() => onAddToCart(id)}>Add to Cart</button>
    </div>
  );
};

// Advantages:
// ✅ Easy to test (just props and callbacks)
// ✅ Reusable in different contexts
// ✅ Easy to style variations
// ✅ Decoupled from business logic
```

---

**Pattern 2: Container Component**

```tsx
// File: components/features/ProductListContainer.tsx
import { useDispatch, useSelector } from 'react-redux';
import { productService } from '../../services/product.service';
import { ProductCard } from '../common/ProductCard';

export const ProductListContainer: React.FC = () => {
  const dispatch = useDispatch();
  const { products, loading } = useSelector(state => state.products);
  
  useEffect(() => {
    // Business logic: fetch products
    productService.getAllProducts()
      .then(data => dispatch(setProducts(data)))
      .catch(err => dispatch(setError(err)));
  }, [dispatch]);
  
  const handleAddToCart = (productId: string) => {
    // Business logic: add to cart
    dispatch(addToCart(productId));
  };
  
  if (loading) return <Spinner />;
  
  return (
    <div className="product-list">
      {products.map(product => (
        <ProductCard
          key={product.id}
          {...product}
          onAddToCart={handleAddToCart}
        />
      ))}
    </div>
  );
};
```

---

### 🏗️ **C. Composition Over Inheritance**

```tsx
// ❌ BAD: Inheritance approach
class BaseInput extends React.Component {
  constructor(props) {
    super(props);
    this.state = { value: '' };
  }
  // Base logic...
}

class EmailInput extends BaseInput {
  validate() { /* email validation */ }
}

class PasswordInput extends BaseInput {
  validate() { /* password validation */ }
}

// Problems:
// - Deep inheritance chains
// - Hard to reuse logic
// - Fragile base class problem
// - Type safety issues in TypeScript

// ✅ GOOD: Composition with custom hooks
const useInput = (initialValue = '') => {
  const [value, setValue] = useState(initialValue);
  
  return {
    value,
    onChange: (e) => setValue(e.target.value),
    reset: () => setValue(''),
  };
};

const useEmailInput = () => {
  const input = useInput('');
  const [error, setError] = useState('');
  
  const validate = () => {
    const isValid = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(input.value);
    setError(isValid ? '' : 'Invalid email');
    return isValid;
  };
  
  return { ...input, validate, error };
};

// Usage - no inheritance, just composition!
const EmailField = () => {
  const email = useEmailInput();
  
  return (
    <div>
      <input {...email} />
      {email.error && <span className="error">{email.error}</span>}
    </div>
  );
};
```

---

## 3. State Management Patterns

### 🎪 **A. When to Use State Management**

```
DON'T use Redux for:
❌ Local UI state (dropdown open/closed, input value)
❌ Temporary state that's not reused
❌ Animation state
❌ Small apps (< 5 components sharing state)

USE Redux for:
✅ Authentication state (token, user info)
✅ App configuration
✅ Cached data (products, orders)
✅ Shared state across many components
✅ Large apps with complex state logic
```

---

### 📦 **B. Redux Architecture (Recommended)**

```tsx
// File: store/slices/productSlice.ts
import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { productService } from '../../services/product.service';

export const fetchProducts = createAsyncThunk(
  'products/fetchProducts',
  async (filters: Filters) => {
    const data = await productService.getAllProducts(filters);
    return data;
  }
);

interface ProductState {
  items: Product[];
  loading: boolean;
  error: string | null;
  selectedProduct: Product | null;
}

const initialState: ProductState = {
  items: [],
  loading: false,
  error: null,
  selectedProduct: null,
};

const productSlice = createSlice({
  name: 'products',
  initialState,
  reducers: {
    selectProduct: (state, action) => {
      state.selectedProduct = action.payload;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchProducts.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchProducts.fulfilled, (state, action) => {
        state.items = action.payload;
        state.loading = false;
      })
      .addCase(fetchProducts.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to fetch products';
      });
  },
});

export const { selectProduct } = productSlice.actions;
export default productSlice.reducer;
```

---

**Using in Components:**

```tsx
// File: components/features/ProductList.tsx
const ProductList = () => {
  const dispatch = useDispatch();
  const { items, loading, error } = useSelector(state => state.products);
  
  useEffect(() => {
    dispatch(fetchProducts(filters));
  }, [dispatch, filters]);
  
  if (loading) return <Spinner />;
  if (error) return <ErrorMessage message={error} />;
  
  return (
    <div>
      {items.map(product => (
        <ProductCard key={product.id} product={product} />
      ))}
    </div>
  );
};
```

---

### 🚀 **C. Zustand (Lightweight Alternative)**

```tsx
// File: store/productStore.ts (if using Zustand instead of Redux)
import { create } from 'zustand';

interface ProductStore {
  items: Product[];
  loading: boolean;
  selectedProduct: Product | null;
  
  // Actions
  setProducts: (products: Product[]) => void;
  selectProduct: (product: Product) => void;
  fetchProducts: () => Promise<void>;
}

export const useProductStore = create<ProductStore>((set) => ({
  items: [],
  loading: false,
  selectedProduct: null,
  
  setProducts: (items) => set({ items }),
  selectProduct: (product) => set({ selectedProduct: product }),
  
  fetchProducts: async () => {
    set({ loading: true });
    try {
      const data = await productService.getAllProducts();
      set({ items: data, loading: false });
    } catch (error) {
      set({ loading: false });
    }
  },
}));
```

---

## 4. SOLID Principles in Frontend

### **A. Single Responsibility Principle**

Each component/hook/service should have ONE reason to change.

```tsx
// ❌ BAD: Multiple responsibilities
const ProductPage = ({ productId }) => {
  // Responsibility 1: API fetching
  const [product, setProduct] = useState(null);
  useEffect(() => {
    fetch(`/api/products/${productId}`)
      .then(res => res.json())
      .then(setProduct);
  }, [productId]);
  
  // Responsibility 2: Validation
  const validatePrice = (price) => price > 0;
  
  // Responsibility 3: Formatting
  const formatPrice = (price) => `$${price.toFixed(2)}`;
  
  // Responsibility 4: UI rendering
  return <div>{formatPrice(product?.price)}</div>;
};

// ✅ GOOD: Each function has ONE responsibility
const useProduct = (productId) => {  // Responsibility: API fetching
  const [product, setProduct] = useState(null);
  useEffect(() => {
    productService.getProduct(productId).then(setProduct);
  }, [productId]);
  return product;
};

const validatePrice = (price) => price > 0;  // Responsibility: Validation

const priceFormatter = {              // Responsibility: Formatting
  format: (price) => `$${price.toFixed(2)}`,
};

const ProductPage = ({ productId }) => {  // Responsibility: UI only
  const product = useProduct(productId);
  return <div>{priceFormatter.format(product?.price)}</div>;
};
```

---

### **B. Open/Closed Principle**

Open for extension, closed for modification.

```tsx
// ❌ BAD: Need to modify for new features
const formatValue = (value, type) => {
  if (type === 'price') return `$${value.toFixed(2)}`;
  if (type === 'date') return new Date(value).toLocaleDateString();
  if (type === 'percentage') return `${(value * 100).toFixed(2)}%`;
  // Every new format type requires modifying this function!
};

// ✅ GOOD: Extend without modifying
const formatters = {
  price: (value) => `$${value.toFixed(2)}`,
  date: (value) => new Date(value).toLocaleDateString(),
  percentage: (value) => `${(value * 100).toFixed(2)}%`,
};

const formatValue = (value, type) => {
  const formatter = formatters[type];
  return formatter?.(value) ?? value;
};

// Adding new format is just extending the object:
formatters.currency = (value, currencyCode) => 
  `${currencyCode} ${value.toFixed(2)}`;
```

---

### **C. Liskov Substitution Principle**

Derived classes/components should be usable in place of base class without breaking.

```tsx
// ✅ GOOD: All buttons are substitutable
interface ButtonProps {
  onClick: () => void;
  children: React.ReactNode;
}

const PrimaryButton: React.FC<ButtonProps> = ({ onClick, children }) => (
  <button className="btn-primary" onClick={onClick}>{children}</button>
);

const SecondaryButton: React.FC<ButtonProps> = ({ onClick, children }) => (
  <button className="btn-secondary" onClick={onClick}>{children}</button>
);

// Both can be used interchangeably:
const DialogFooter = ({ Button }: { Button: React.ComponentType<ButtonProps> }) => (
  <div>
    <Button onClick={() => console.log('Cancel')}>Cancel</Button>
    <Button onClick={() => console.log('OK')}>OK</Button>
  </div>
);
```

---

### **D. Interface Segregation Principle**

Create fine-grained interfaces, don't force clients to depend on methods they don't use.

```tsx
// ❌ BAD: Fat interface
interface User {
  id: string;
  name: string;
  email: string;
  password: string;    // Not all components need this!
  phoneNumber: string; // Not all components need this!
  address: string;     // Not all components need this!
  role: string;
}

// ✅ GOOD: Segregated interfaces
interface UserProfile {
  id: string;
  name: string;
  email: string;
}

interface UserAuth {
  password: string;
  role: string;
}

interface UserContactInfo {
  phoneNumber: string;
  address: string;
}

// Each component gets only what it needs:
const UserCard: React.FC<{ user: UserProfile }> = ({ user }) => (
  <div>{user.name}</div>
);

const AdminPanel: React.FC<{ user: UserAuth }> = ({ user }) => (
  <div>{user.role}</div>
);
```

---

### **E. Dependency Inversion Principle**

Depend on abstractions, not concrete implementations.

```tsx
// ❌ BAD: Tightly coupled to specific service
const ProductList = () => {
  useEffect(() => {
    // Direct import of concrete implementation
    productService.getAllProducts()
      .then(setProducts);
  }, []);
};

// ✅ GOOD: Depend on abstraction
interface IProductService {
  getAllProducts(): Promise<Product[]>;
}

const ProductList: React.FC<{ service: IProductService }> = ({ service }) => {
  useEffect(() => {
    service.getAllProducts().then(setProducts);
  }, [service]);
};

// Easy to swap implementations:
// For production: <ProductList service={httpProductService} />
// For testing: <ProductList service={mockProductService} />
```

---

## 5. Design Patterns for Frontend

### **A. Custom Hooks Pattern**

Custom hooks are the React way of extracting logic for reuse.

```tsx
// File: hooks/useApi.ts
interface UseApiState<T> {
  data: T | null;
  loading: boolean;
  error: string | null;
}

export const useApi = <T,>(
  url: string,
  options?: RequestInit
): UseApiState<T> & { refetch: () => Promise<void> } => {
  const [state, setState] = useState<UseApiState<T>>({
    data: null,
    loading: true,
    error: null,
  });
  
  const fetch = useCallback(async () => {
    try {
      setState(prev => ({ ...prev, loading: true, error: null }));
      const response = await apiClient.get<T>(url, options);
      setState({ data: response, loading: false, error: null });
    } catch (err) {
      setState(prev => ({
        ...prev,
        error: (err as Error).message,
        loading: false,
      }));
    }
  }, [url, options]);
  
  useEffect(() => {
    fetch();
  }, [fetch]);
  
  return { ...state, refetch: fetch };
};

// Usage:
const ProductPage = ({ productId }) => {
  const { data: product, loading, error, refetch } = useApi<Product>(
    `/api/products/${productId}`
  );
  
  if (loading) return <Spinner />;
  if (error) return <Error message={error} onRetry={refetch} />;
  
  return <ProductDetail product={product} />;
};
```

---

### **B. Render Prop Pattern**

Pass rendering logic as a prop function.

```tsx
// File: components/DataFetcher.tsx
interface DataFetcherProps<T> {
  url: string;
  children: (state: UseApiState<T>) => React.ReactNode;
}

export const DataFetcher = <T,>({ url, children }: DataFetcherProps<T>) => {
  const state = useApi<T>(url);
  return <>{children(state)}</>;
};

// Usage:
<DataFetcher url="/api/products/123">
  {({ data, loading, error }) => (
    loading ? <Spinner /> :
    error ? <Error message={error} /> :
    <ProductDetail product={data} />
  )}
</DataFetcher>
```

---

### **C. Higher-Order Component (HOC)**

Wrap a component to add functionality.

```tsx
// File: hoc/withAuth.tsx
const withAuth = <P extends object>(
  Component: React.ComponentType<P>
): React.FC<P> => {
  return (props: P) => {
    const { user, loading } = useAuth();
    
    if (loading) return <Spinner />;
    if (!user) return <Navigate to="/login" />;
    
    return <Component {...props} />;
  };
};

// Usage:
const ProtectedPage = withAuth(MyComponent);
// Now <ProtectedPage /> automatically checks authentication
```

---

### **D. Observer Pattern (Event Bus)**

Decouple components using event emitters.

```tsx
// File: utils/eventBus.ts
type Eventlistener<T> = (data: T) => void;

export class EventBus {
  private listeners: Map<string, EventListener<any>[]> = new Map();
  
  on<T>(event: string, listener: EventListener<T>) {
    if (!this.listeners.has(event)) {
      this.listeners.set(event, []);
    }
    this.listeners.get(event)!.push(listener);
  }
  
  emit<T>(event: string, data: T) {
    this.listeners.get(event)?.forEach(listener => listener(data));
  }
}

export const eventBus = new EventBus();

// Usage:
// Component 1: Emit event
const handleAddToCart = (product) => {
  eventBus.emit('product:added', product);
};

// Component 2: Listen to event
useEffect(() => {
  const handler = (product) => {
    showNotification(`${product.name} added to cart!`);
  };
  eventBus.on('product:added', handler);
}, []);
```

---

## 6. Frontend Security (الأمان)

### 🔒 **A. Common Security Vulnerabilities**

| Vulnerability | Example | Risk |
|---|---|---|
| **XSS** (Cross-Site Scripting) | `<div>{userInput}</div>` | Attacker injects malicious JS |
| **CSRF** (Cross-Site Request Forgery) | Missing token validation | Unauthorized actions on user's behalf |
| **SSRF** (Server-Side Request Forgery) | Unvalidated API requests | Access internal resources |
| **SQL Injection** | Direct query concatenation | Database compromise |
| **Sensitive Data Exposure** | API keys in code/localStorage | Token theft, data breach |

---

### 🛡️ **B. XSS Prevention**

```tsx
// ❌ BAD: Direct HTML injection
const renderComment = (comment: string) => {
  return <div dangerouslySetInnerHTML={{ __html: comment }} />;
  // If comment = "<img onerror='alert(1)'>" → Attack!
};

// ✅ GOOD: React auto-escapes by default
const renderComment = (comment: string) => {
  return <div>{comment}</div>; // Safe!
  // Output: "<img onerror='alert(1)'>" (as text, not HTML)
};

// ✅ If you MUST use HTML (from trusted source):
import DOMPurify from 'dompurify';

const renderSafeHtml = (html: string) => {
  const cleanHtml = DOMPurify.sanitize(html);
  return <div dangerouslySetInnerHTML={{ __html: cleanHtml }} />;
};
```

---

### 🔐 **C. Secure Token Handling**

```tsx
// ❌ BAD: Token in localStorage (vulnerable to XSS)
localStorage.setItem('authToken', token);
// XSS attack: JS reads localStorage and sends token to attacker

// ✅ GOOD: Token in HttpOnly cookie (cannot be accessed via JS)
// Server sets: Set-Cookie: authToken=xyz; HttpOnly; Secure; SameSite=Strict
// Browser automatically sends in requests
// XSS attack cannot steal the token!

// For API calls, if using Axios:
const apiClient = axios.create({
  baseURL: 'https://api.example.com',
  withCredentials: true, // Send cookies automatically
});

// ✅ Alternative: Store in memory (lost on refresh)
let authToken: string | null = null;

export const setAuthToken = (token: string) => {
  authToken = token;
};

export const getAuthToken = () => authToken;

// Refresh token from server on app load:
useEffect(() => {
  authService.refreshToken() // Server checks HttpOnly refresh token
    .then(newAccessToken => setAuthToken(newAccessToken));
}, []);
```

---

### 🔑 **D. CSRF Protection**

```tsx
// ✅ Backend provides CSRF token
const useCSRFToken = () => {
  const [token, setToken] = useState('');
  
  useEffect(() => {
    // Fetch CSRF token from backend
    fetch('/api/csrf-token')
      .then(res => res.json())
      .then(({ token }) => setToken(token));
  }, []);
  
  return token;
};

// ✅ Include token in every state-changing request
const createOrder = async (orderData) => {
  const csrfToken = useCSRFToken();
  
  const response = await fetch('/api/orders', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'X-CSRF-Token': csrfToken, // Include token
    },
    body: JSON.stringify(orderData),
  });
  
  return response.json();
};

// ✅ Backend validates token matches session
// Server checks: X-CSRF-Token header == CSRF token in session
// If attacker tries to make request without valid token → Rejected!
```

---

### 🔍 **E. Input Validation & Sanitization**

```tsx
// File: utils/validators.ts
export const validators = {
  email: (email: string): boolean => 
    /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email),
  
  password: (password: string): boolean => 
    password.length >= 8 && /[A-Z]/.test(password) && /[0-9]/.test(password),
  
  url: (url: string): boolean => {
    try {
      new URL(url);
      return true;
    } catch {
      return false;
    }
  },
};

// File: utils/sanitizers.ts
export const sanitizers = {
  text: (text: string): string => 
    DOMPurify.sanitize(text, { ALLOWED_TAGS: [] }),
  
  html: (html: string): string =>
    DOMPurify.sanitize(html), // Allow safe tags
  
  filename: (filename: string): string =>
    filename.replace(/[^a-zA-Z0-9.-]/g, '_'),
};

// Usage in form:
const [email, setEmail] = useState('');
const [emailError, setEmailError] = useState('');

const handleEmailChange = (e) => {
  const value = e.target.value;
  setEmail(value);
  
  if (!validators.email(value)) {
    setEmailError('Invalid email format');
  } else {
    setEmailError('');
  }
};

const handleSubmit = (e) => {
  e.preventDefault();
  
  if (!validators.email(email)) {
    setEmailError('Invalid email');
    return;
  }
  
  // Backend validation is also required!
  submitForm({ email });
};
```

---

### 🚫 **F. Content Security Policy (CSP)**

```html
<!-- In index.html or server header -->
<meta 
  http-equiv="Content-Security-Policy" 
  content="
    default-src 'self';
    script-src 'self' 'unsafe-inline' https://cdn.example.com;
    style-src 'self' 'unsafe-inline';
    img-src 'self' data: https:;
    font-src 'self' data:;
    connect-src 'self' https://api.example.com;
    frame-ancestors 'none';
  "
/>
```

**What it does:**
- `default-src 'self'` → Only load resources from own domain
- `script-src 'self'` → Only run scripts from own domain (prevents inline script injection)
- `connect-src https://api.example.com` → Only allow API calls to your backend
- `frame-ancestors 'none'` → Can't be framed by other sites (clickjacking protection)

---

## 7. Responsive Design & Mobile-First (الاستجابة والموبايل)

### 📱 **A. Mobile-First Philosophy**

**Mobile-First = Start Small, Build Up**

```css
/* ❌ BAD: Desktop-First (write desktop, then hide on mobile)
@media (max-width: 768px) {
  .hide-on-mobile { display: none; }
}
*/

/* ✅ GOOD: Mobile-First (write mobile, then enhance on desktop)
.mobile-menu { display: block; }

@media (min-width: 768px) {
  .mobile-menu { display: none; }
  .desktop-menu { display: block; }
}
*/

/* Why Mobile-First?
1. Performance: Load less CSS initially
2. Progressive Enhancement: Add features as screen size increases
3. User Base: 60%+ users on mobile
4. Faster to default (mobile) than to override (desktop)
*/
```

---

### 📐 **B. Responsive Breakpoints**

```tsx
// File: styles/breakpoints.ts
export const breakpoints = {
  mobile: 320,      // iPhone SE, small phones
  tablet: 768,      // iPad, tablets
  desktop: 1024,    // Small desktop
  wide: 1440,       // Full desktop
  ultraWide: 1920,  // Large monitors
};

export const media = {
  mobile: `@media (min-width: ${breakpoints.mobile}px)`,
  tablet: `@media (min-width: ${breakpoints.tablet}px)`,
  desktop: `@media (min-width: ${breakpoints.desktop}px)`,
  wide: `@media (min-width: ${breakpoints.wide}px)`,
};

// Usage with styled-components:
const Container = styled.div`
  width: 100%;           /* Mobile default: full width */
  padding: 8px;
  
  ${media.tablet} {      /* Tablet: add padding */
    padding: 16px;
    width: 750px;
  }
  
  ${media.desktop} {     /* Desktop: wider */
    width: 970px;
    padding: 24px;
  }
`;
```

---

### 🎯 **C. Responsive Layout Patterns**

**Pattern 1: Flexbox for Simple Layouts**

```tsx
// ✅ GOOD: Responsive grid using Flexbox
const ProductGrid = styled.div`
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  
  /* Mobile: 1 column */
  & > * {
    flex: 0 0 100%;  // 100% width
  }
  
  /* Tablet: 2 columns */
  ${media.tablet} {
    & > * {
      flex: 0 0 calc(50% - 8px);  // 2 columns (minus gap)
    }
  }
  
  /* Desktop: 3 columns */
  ${media.desktop} {
    & > * {
      flex: 0 0 calc(33.333% - 11px);  // 3 columns
    }
  }
  
  /* Wide: 4 columns */
  ${media.wide} {
    & > * {
      flex: 0 0 calc(25% - 12px);  // 4 columns
    }
  }
`;
```

**Pattern 2: CSS Grid for Complex Layouts**

```tsx
const DashboardLayout = styled.div`
  display: grid;
  grid-template-columns: 1fr;  /* Mobile: single column */
  gap: 16px;
  
  ${media.desktop} {
    grid-template-columns: 250px 1fr;  /* Desktop: sidebar + content */
    
    grid-template-areas:
      "sidebar header"
      "sidebar main"
      "sidebar footer";
  }
`;

const Sidebar = styled.aside`
  grid-area: sidebar;
  position: fixed;           /* Mobile: overlay */
  bottom: 0;
  left: 0;
  right: 0;
  height: 60px;
  
  ${media.desktop} {
    position: static;        /* Desktop: static */
    height: auto;
  }
`;
```

---

### 🖼️ **D. Responsive Images**

```tsx
// ❌ BAD: Same image for all devices
<img src="hero-1920x1080.jpg" alt="Hero" />  // 2-5MB for mobile users!

// ✅ GOOD: Picture tag with multiple sources
<picture>
  {/* Mobile: Small optimization */}
  <source 
    srcSet="hero-mobile.webp 480w, hero-mobile@2x.webp 960w"
    media="(max-width: 640px)"
    type="image/webp"
  />
  <source 
    srcSet="hero-mobile.jpg 480w, hero-mobile@2x.jpg 960w"
    media="(max-width: 640px)"
  />
  
  {/* Tablet: Medium image */}
  <source 
    srcSet="hero-tablet.webp 768w"
    media="(max-width: 1024px)"
    type="image/webp"
  />
  <source 
    srcSet="hero-tablet.jpg 768w"
    media="(max-width: 1024px)"
  />
  
  {/* Desktop: Full resolution */}
  <source srcSet="hero-desktop.webp" type="image/webp" />
  
  {/* Fallback */}
  <img 
    src="hero-desktop.jpg" 
    alt="Hero"
    loading="lazy"
  />
</picture>

// Or using Next.js Image component (auto-optimizes):
<Image
  src={heroImage}
  alt="Hero"
  sizes="(max-width: 640px) 480px,
         (max-width: 1024px) 768px,
         1920px"
  responsive
/>
```

**File Size Impact:**
```
3G Network (2Mbps):
  Mobile (480px): 50KB × 2Mbps = 200ms ✅
  Desktop (1920px): 500KB × 2Mbps = 2000ms ❌

Save 90% bandwidth on mobile!
```

---

### 👆 **E. Touch Interactions**

```tsx
// ❌ BAD: Hover-only interactions
const Button = styled.button`
  padding: 8px;  // Too small for touch
  
  &:hover {
    background-color: blue;  // Mobile users can't see this!
  }
`;

// ✅ GOOD: Touch-friendly
const Button = styled.button`
  padding: 16px 24px;  /* 44px minimum tap target (Apple guideline) */
  border-radius: 8px;
  
  /* Hover for desktop */
  @media (hover: hover) {
    &:hover {
      background-color: blue;
    }
  }
  
  /* Active state for mobile (visual feedback) */
  &:active {
    background-color: darkblue;
    transform: scale(0.98);  /* Haptic-like feedback */
  }
`;

// ❌ BAD: Swipe not supported
const ProductCarousel = ({ products }) => {
  return (
    <div>
      {products.map(p => (
        <div key={p.id}>{p.name}</div>
      ))}
    </div>
  );
};

// ✅ GOOD: Touch swipe support
import { useSwipeable } from 'react-swipeable';

const ProductCarousel = ({ products }) => {
  const [index, setIndex] = useState(0);
  
  const { ref } = useSwipeable({
    onSwipedLeft: () => setIndex((i) => (i + 1) % products.length),
    onSwipedRight: () => setIndex((i) => (i - 1 + products.length) % products.length),
  });
  
  return (
    <div ref={ref}>
      {products[index].name}
    </div>
  );
};
```

---

### 📊 **F. Responsive Typography**

```css
/* ❌ BAD: Fixed font size */
h1 {
  font-size: 32px;  /* Too small on mobile, wastes space */
}

/* ✅ GOOD: Fluid typography */
h1 {
  font-size: clamp(24px, 5vw, 48px);
  /* Min: 24px (mobile) */
  /* Preferred: 5% of viewport width (scales) */
  /* Max: 48px (desktop) */
}

p {
  font-size: clamp(14px, 2vw, 18px);
  line-height: 1.6;  /* Crucial for readability */
}

/* Alternative: Media queries */
h1 {
  font-size: 20px;
}

@media (min-width: 768px) {
  h1 {
    font-size: 32px;
  }
}

@media (min-width: 1024px) {
  h1 {
    font-size: 48px;
  }
}
```

---

### 🧪 **G. Responsive Testing**

```tsx
// File: __tests__/ProductCard.responsive.test.tsx
import { render, screen } from '@testing-library/react';
import { ProductCard } from '../ProductCard';

describe('ProductCard - Responsive', () => {
  // Test mobile viewport
  it('should stack vertically on mobile', () => {
    window.matchMedia = jest.fn().mockImplementation(query => ({
      matches: query === '(max-width: 640px)',
      media: query,
      onchange: null,
      addListener: jest.fn(),
      removeListener: jest.fn(),
      addEventListener: jest.fn(),
      removeEventListener: jest.fn(),
      dispatchEvent: jest.fn(),
    }));
    
    render(<ProductCard product={mockProduct} />);
    
    const image = screen.getByRole('img');
    expect(image).toHaveStyle('width: 100%');  // Full width on mobile
  });
  
  // Test tablet viewport
  it('should display in 2-column grid on tablet', () => {
    // Mock tablet breakpoint
    expect(screen.getByTestId('product-grid'))
      .toHaveStyle('grid-template-columns: repeat(2, 1fr)');
  });
  
  // Test touch interactions
  it('should handle swipe on mobile', () => {
    const { container } = render(<ProductCarousel />);
    const carousel = container.firstChild;
    
    // Simulate swipe
    fireEvent.touchStart(carousel, { touches: [{ clientX: 100 }] });
    fireEvent.touchEnd(carousel, { changedTouches: [{ clientX: 20 }] });
    
    // Should show next product
    expect(screen.getByText('Product 2')).toBeVisible();
  });
});
```

---

### ⚙️ **H. Mobile Performance**

```tsx
// ❌ BAD: Expensive operations on mobile
const ProductList = () => {
  const [products, setProducts] = useState([]);
  
  // Complex filtering on every render = slow on mobile
  const filtered = products.filter(p => 
    p.name.toLowerCase().includes(search) &&
    p.price > minPrice &&
    p.price < maxPrice &&
    p.category === selectedCategory
  );
};

// ✅ GOOD: Virtual scrolling for long lists
import { FixedSizeList as List } from 'react-window';

const ProductList = ({ products }) => {
  return (
    <List
      height={600}
      itemCount={products.length}
      itemSize={100}
      width="100%"
    >
      {({ index, style }) => (
        <div style={style}>
          <ProductCard product={products[index]} />
        </div>
      )}
    </List>
  );
};

// Only renders visible items!
// 10,000 products → renders ~10 items
// Desktop: 8 items, Mobile: 5 items
```

---

### 📱 **I. Responsive Design Checklist**

```markdown
## Mobile-First Implementation

- [ ] All text readable without zooming (16px+ for body)
- [ ] Touch targets 44x44px minimum
- [ ] Enough spacing between interactive elements
- [ ] Images optimized for mobile (WebP, responsive srcset)
- [ ] Hamburger menu for navigation on mobile
- [ ] Horizontal scrolling prevented (no overflow-x)
- [ ] Form inputs properly sized (not tiny)
- [ ] No infinite scrolls (pagination for pagination)
- [ ] Viewport meta tag set correctly
- [ ] No fixed layouts (use flexbox/grid)

## Tablet (768px+)

- [ ] 2-column layouts where appropriate
- [ ] Larger images
- [ ] Optimize touch: larger buttons
- [ ] Side navigation instead of hamburger

## Desktop (1024px+)

- [ ] Multi-column layouts (3-4 columns)
- [ ] Full-size images
- [ ] Hover states enabled
- [ ] Larger click targets (but not huge)

## Testing

- [ ] Test on actual devices (iOS/Android)
- [ ] Test on different browsers (Safari, Chrome, Firefox)
- [ ] Test network speeds (3G, 4G, WiFi)
- [ ] Test orientations (portrait, landscape)
- [ ] Use Chrome DevTools device emulation
```

---

### 🎨 **J. CSS Frameworks for Responsive Design**

**Tailwind CSS (Utility-Based):**
```tsx
// Mobile-first utilities built-in
<div className="
  grid 
  grid-cols-1              /* Mobile: 1 column */
  md:grid-cols-2          /* Tablet: 2 columns */
  lg:grid-cols-3          /* Desktop: 3 columns */
  gap-4
  p-4 md:p-6 lg:p-8       /* Responsive padding */
">
  {/* Content */}
</div>
```

**Bootstrap (Component-Based):**
```tsx
<div className="
  container
  row
">
  <div className="col-12 col-md-6 col-lg-4">
    Responsive columns
  </div>
</div>
```

**CSS Grid Template Areas (Custom):**
```tsx
const Layout = styled.div`
  display: grid;
  gap: 16px;
  
  /* Mobile: single column */
  grid-template-areas:
    "header"
    "nav"
    "main"
    "footer";
  
  /* Desktop: sidebar + main */
  @media (min-width: 1024px) {
    grid-template-columns: 250px 1fr;
    grid-template-areas:
      "header header"
      "nav main"
      "footer footer";
  }
`;
```

---

## 8. Frontend Performance (الأداء)

**Problem:**
```
Large JavaScript bundle = Slow download + Slow parsing + Slow execution

Example:
Bundle size: 500KB (gzipped)
Network: 4G = 50KB/s
Download time: 10 seconds
Parse + Execute: 5 seconds
Total: 15 seconds before app is interactive! 💀
```

**Solution 1: Code Splitting**

```tsx
// ❌ BAD: All code in one bundle
import ProductDetail from './pages/ProductDetail';
import OrderPage from './pages/OrderPage';
import AdminPanel from './pages/AdminPanel';

// All pages loaded upfront, even if user never visits them

// ✅ GOOD: Lazy load pages
import { lazy, Suspense } from 'react';

const ProductDetail = lazy(() => import('./pages/ProductDetail'));
const OrderPage = lazy(() => import('./pages/OrderPage'));
const AdminPanel = lazy(() => import('./pages/AdminPanel'));

// Chunks:
// - main.js: 50KB (core app)
// - pages_ProductDetail.js: 100KB (loaded when needed)
// - pages_OrderPage.js: 80KB (loaded when needed)
// - pages_AdminPanel.js: 120KB (loaded when needed)

const Router = () => (
  <Routes>
    <Route path="/products/:id" element={
      <Suspense fallback={<Spinner />}>
        <ProductDetail />
      </Suspense>
    } />
  </Routes>
);
```

---

**Solution 2: Tree Shaking**

```tsx
// ❌ BAD: Default import (whole module loaded)
import _ from 'lodash';
const list = _.map([1, 2, 3], x => x * 2);
// Bundle includes entire lodash library (~70KB)

// ✅ GOOD: Named import (only needed function)
import { map } from 'lodash-es';
const list = map([1, 2, 3], x => x * 2);
// Bundler removes unused functions via tree-shaking

// Or use lighter alternative:
const map = (arr, fn) => arr.map(fn);
```

---

**Solution 3: Dynamic Imports**

```tsx
// ✅ Load library only when needed
const ExcelExporter = lazy(() => 
  import('xlsx').then(module => ({
    default: module.utils
  }))
);

// ✅ Conditional import
const getPrintLibrary = async () => {
  if (typeof window !== 'undefined') {
    const jsPDF = await import('jspdf');
    return jsPDF;
  }
};

// Usage:
const handleExport = async () => {
  const xlsx = await import('xlsx');
  xlsx.utils.sheet_to_csv(data);
};
```

---

### 🖼️ **B. Image Optimization**

```tsx
// ❌ BAD: Large unoptimized images
<img src="product.jpg" alt="Product" />
// 5MB JPG → User downloads full-res image

// ✅ GOOD: Multiple formats and sizes
<picture>
  <source srcSet="product.webp" type="image/webp" />
  <source srcSet="product.jpg" type="image/jpeg" />
  <img 
    src="product.jpg" 
    alt="Product"
    loading="lazy"  // Lazy load below fold
    width="300"
    height="300"
  />
</picture>

// Generate multiple sizes:
// - product-small.jpg (200x200, 50KB)
// - product-medium.jpg (400x400, 150KB)
// - product-large.jpg (800x800, 300KB)

// Responsive images:
<img
  srcSet="
    product-small.jpg 200w,
    product-medium.jpg 400w,
    product-large.jpg 800w
  "
  sizes="(max-width: 600px) 200px, 400px"
  src="product.jpg"
  alt="Product"
/>

// What happens:
// - Mobile (300px): Downloads product-small.jpg (50KB)
// - Tablet (600px): Downloads product-medium.jpg (150KB)
// - Desktop (1000px): Downloads product-large.jpg (300KB)
```

---

### 🎯 **C. Core Web Vitals**

```
Metrics that Google uses to rank websites:

1. LCP (Largest Contentful Paint) < 2.5s
   - When main content appears on screen
   - Indicates perceived page load speed

2. FID (First Input Delay) < 100ms
   - Response time to user interaction
   - Indicates page responsiveness

3. CLS (Cumulative Layout Shift) < 0.1
   - How much layout shifts during loading
   - Indicates visual stability
```

---

**Optimize LCP:**

```tsx
// ❌ BAD: Large hero image blocks rendering
const HomePage = () => (
  <div>
    <img src="hero-5mb.jpg" alt="Hero" />
    {/* Rest of page won't render until image loads */}
  </div>
);

// ✅ GOOD: Prioritize hero image
const HomePage = () => (
  <div>
    <img 
      src="hero.jpg" 
      alt="Hero"
      fetchPriority="high"  // Prioritize this image
      width="1200"
      height="600"
    />
  </div>
);

// Or use priority prop in Next.js:
<Image
  src={heroImage}
  alt="Hero"
  priority  // Preload this image
  width={1200}
  height={600}
/>
```

---

**Optimize FID:**

```tsx
// ❌ BAD: Heavy computation on main thread
const handleSort = (items) => {
  // Complex sort algorithm blocks UI for 500ms
  const sorted = complexSort(items);
  setItems(sorted);
};

// ✅ GOOD: Offload to Web Worker
// File: workers/sort.worker.ts
self.onmessage = (event) => {
  const sorted = complexSort(event.data);
  self.postMessage(sorted);
};

// Main thread:
const handleSort = (items) => {
  const worker = new Worker(new URL('./workers/sort.worker.ts', import.meta.url));
  worker.onmessage = (event) => {
    setItems(event.data);
    worker.terminate();
  };
  worker.postMessage(items);
};

// UI stays responsive while worker computes!
```

---

**Optimize CLS:**

```tsx
// ❌ BAD: Images without dimensions cause layout shift
<div>
  <img src="product.jpg" alt="Product" />
  {/* Page layout shifts when image loads */}
</div>

// ✅ GOOD: Reserve space with aspect ratio
<div style={{ aspectRatio: '1' }}>
  <img src="product.jpg" alt="Product" />
</div>

// Or explicit dimensions:
<img 
  src="product.jpg" 
  alt="Product"
  width="300"
  height="300"
/>

// Or with container queries:
.image-container {
  width: 300px;
  height: 300px;
  overflow: hidden;
}
```

---

### 💾 **D. Caching Strategies**

**Browser Caching:**

```tsx
// Server headers (set by backend):
// Cache-Control: public, max-age=31536000
// For: Static assets (JS, CSS, images)
// Benefit: No request needed, instant load from disk

// Cache-Control: public, max-age=3600
// For: Semi-static content (HTML pages)
// Benefit: 1 hour of cached pages

// Cache-Control: private, max-age=1800
// For: User-specific content (account page)
// Benefit: User's device caches but not shared caches

// Cache-Control: no-cache, no-store
// For: Sensitive data (API responses with tokens)
// Benefit: Always fresh, never cached
```

---

**Service Worker Caching:**

```tsx
// File: public/sw.js
const CACHE_NAME = 'app-v1';

self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(CACHE_NAME).then((cache) => 
      cache.addAll([
        '/',
        '/index.html',
        '/styles.css',
        '/app.js',
      ])
    )
  );
});

self.addEventListener('fetch', (event) => {
  // Cache-first: Use cache, fallback to network
  event.respondWith(
    caches.match(event.request).then((response) => 
      response || fetch(event.request)
    )
  );
});

// Register in React:
useEffect(() => {
  if ('serviceWorker' in navigator) {
    navigator.serviceWorker.register('/sw.js');
  }
}, []);

// Benefits:
// ✅ Instant loads (no network request)
// ✅ Works offline
// ✅ Faster on slow networks
```

---

### 🔄 **E. Render Performance**

**Memoization (Prevent Unnecessary Re-renders):**

```tsx
// ❌ BAD: ProductCard re-renders even if props haven't changed
const ProductCard = ({ product, onAddToCart }) => {
  console.log('ProductCard rendered');
  return (
    <div>
      <h3>{product.name}</h3>
      <button onClick={() => onAddToCart(product.id)}>Add</button>
    </div>
  );
};

// Parent re-renders → All ProductCard children re-render
const ProductList = () => {
  const [filter, setFilter] = useState('');
  
  return (
    <div>
      <input value={filter} onChange={e => setFilter(e.target.value)} />
      {/* Every input change re-renders all ProductCards! */}
      {products.map(p => <ProductCard key={p.id} product={p} />)}
    </div>
  );
};

// ✅ GOOD: Memoize to prevent re-renders
const ProductCard = memo(({ product, onAddToCart }) => {
  return (
    <div>
      <h3>{product.name}</h3>
      <button onClick={() => onAddToCart(product.id)}>Add</button>
    </div>
  );
}, (prevProps, nextProps) => 
  // Custom comparison (if props are same, don't re-render)
  prevProps.product.id === nextProps.product.id
);

// Only re-renders when product or callback changes!
```

---

## 9. Frontend Testing Strategy

### 🧪 **A. Testing Pyramid**

```
                    E2E Tests (5%)
                    Slow, Brittle
                    (Playwright, Cypress)
                    
                Integration Tests (35%)
                Medium speed
                (React Testing Library)
                
        Unit Tests (60%)
        Fast, Independent
        (Jest, Vitest)
```

---

### 🔬 **B. Unit Testing**

```tsx
// File: __tests__/priceFormatter.test.ts
import { priceFormatter } from '../utils/formatters';

describe('priceFormatter', () => {
  it('should format price correctly', () => {
    expect(priceFormatter.format(99.5)).toBe('$99.50');
  });
  
  it('should handle zero', () => {
    expect(priceFormatter.format(0)).toBe('$0.00');
  });
  
  it('should handle large numbers', () => {
    expect(priceFormatter.format(1000000)).toBe('$1,000,000.00');
  });
});

// Coverage targets:
// - Statements: 80%+
// - Branches: 75%+
// - Functions: 80%+
// - Lines: 80%+
```

---

### 🔗 **C. Integration Testing**

```tsx
// File: __tests__/ProductCard.test.tsx
import { render, screen, fireEvent } from '@testing-library/react';
import { ProductCard } from '../components/ProductCard';

describe('ProductCard', () => {
  it('should render product details', () => {
    const product = { id: '1', name: 'Test Product', price: 99.99 };
    
    render(
      <ProductCard product={product} onAddToCart={() => {}} />
    );
    
    expect(screen.getByText('Test Product')).toBeInTheDocument();
    expect(screen.getByText('$99.99')).toBeInTheDocument();
  });
  
  it('should call onAddToCart when button clicked', () => {
    const onAddToCart = jest.fn();
    const product = { id: '1', name: 'Test', price: 99.99 };
    
    render(
      <ProductCard product={product} onAddToCart={onAddToCart} />
    );
    
    fireEvent.click(screen.getByText('Add to Cart'));
    
    expect(onAddToCart).toHaveBeenCalledWith('1');
  });
});
```

---

### 🎬 **D. E2E Testing (Cypress)**

```typescript
// File: cypress/e2e/shopping.cy.ts
describe('Shopping Flow', () => {
  it('user can browse and add products to cart', () => {
    // Visit home page
    cy.visit('http://localhost:3000');
    
    // Search for product
    cy.get('[data-testid="search-input"]').type('Laptop');
    cy.get('[data-testid="search-btn"]').click();
    
    // Product appears
    cy.get('[data-testid="product-card"]').first().should('be.visible');
    
    // Add to cart
    cy.get('[data-testid="add-cart-btn"]').first().click();
    
    // Verify notification
    cy.get('[data-testid="toast-notification"]')
      .should('contain', 'Added to cart');
    
    // Go to checkout
    cy.get('[data-testid="cart-icon"]').click();
    cy.get('[data-testid="checkout-btn"]').click();
    
    // Fill form
    cy.get('[name="email"]').type('test@example.com');
    cy.get('[name="card"]').type('4532015112830366');
    
    // Submit
    cy.get('[data-testid="pay-btn"]').click();
    
    // Verify order
    cy.url().should('include', '/order');
    cy.get('[data-testid="order-confirmed"]')
      .should('contain', 'Order Confirmed');
  });
});
```

---

## 10. Observability & Analytics

### 📊 **A. Error Tracking (Sentry)**

```tsx
// File: config/sentry.ts
import * as Sentry from "@sentry/react";

Sentry.init({
  dsn: process.env.REACT_APP_SENTRY_DSN,
  environment: process.env.NODE_ENV,
  tracesSampleRate: 1.0,
  integrations: [
    new Sentry.Replay({
      maskAllText: true,
      blockAllMedia: true,
    }),
  ],
});

// Wrap app
const App = () => (
  <Sentry.ErrorBoundary>
    <Router />
  </Sentry.ErrorBoundary>
);

// Capture errors manually:
try {
  risky Operation();
} catch (error) {
  Sentry.captureException(error, {
    tags: {
      section: 'checkout',
      action: 'payment',
    },
  });
}

// Benefits:
// ✅ Track all JS errors automatically
// ✅ Session replay (see what user was doing)
// ✅ Sourcemap support (original code in stack trace)
// ✅ Performance monitoring
```

---

### 📈 **B. Performance Monitoring**

```tsx
// File: utils/performance.ts
export const reportWebVitals = (metric) => {
  // Send to analytics service
  const body = JSON.stringify(metric);
  navigator.sendBeacon('/api/metrics', body);
  
  // Or use Google Analytics:
  window.gtag?.('event', 'web_vitals', {
    event_category: 'Web Vitals',
    value: Math.round(metric.value),
    event_label: metric.id,
    non_interaction: true,
  });
};

// Usage in React:
import { getCLS, getFID, getFCP, getLCP, getTTFB } from 'web-vitals';

useEffect(() => {
  getCLS(reportWebVitals);
  getFID(reportWebVitals);
  getFCP(reportWebVitals);
  getLCP(reportWebVitals);
  getTTFB(reportWebVitals);
}, []);
```

---

### 🎯 **C. User Analytics**

```tsx
// File: utils/analytics.ts
export const analytics = {
  track: (event: string, properties?: Record<string, any>) => {
    // Send to Segment/Mixpanel/GA4
    window.gtag?.('event', event, properties);
  },
  
  identify: (userId: string, traits?: Record<string, any>) => {
    window.gtag?.('set', { 'user_id': userId });
  },
};

// Usage:
const handleProductView = (productId) => {
  analytics.track('product_viewed', {
    product_id: productId,
    timestamp: new Date().toISOString(),
  });
};

const handleAddToCart = (productId, price) => {
  analytics.track('add_to_cart', {
    product_id: productId,
    value: price,
    currency: 'USD',
  });
};

const handlePurchase = (orderId, total) => {
  analytics.track('purchase', {
    transaction_id: orderId,
    value: total,
    currency: 'USD',
  });
};
```

---

## 11. Deployment & CI/CD for Frontend

### 🐳 **A. Docker for Frontend (React)**

```dockerfile
# File: Dockerfile

# Stage 1: Build
FROM node:20-alpine AS builder

WORKDIR /app

# Copy package files
COPY package*.json ./

# Install dependencies
RUN npm ci --only=production && \
    npm install --only=dev

# Copy source
COPY . .

# Build React app
RUN npm run build

# Stage 2: Runtime
FROM node:20-alpine

WORKDIR /app

# Install serve to run static app
RUN npm install -g serve

# Copy build from builder
COPY --from=builder /app/build ./build

# Health check
HEALTHCHECK --interval=30s --timeout=3s CMD curl -f http://localhost:3000 || exit 1

# Expose port
EXPOSE 3000

# Run
CMD ["serve", "-s", "build", "-l", "3000"]
```

---

### 🚀 **B. GitHub Actions for Frontend**

```yaml
# File: .github/workflows/frontend-deploy.yml

name: Frontend Deploy

on:
  push:
    branches: [main]
    paths:
      - 'frontend/**'
      - '.github/workflows/frontend-deploy.yml'

jobs:
  build-and-test:
    runs-on: ubuntu-latest
    
    steps:
      - uses: actions/checkout@v3
      
      - uses: actions/setup-node@v3
        with:
          node-version: '20'
          cache: 'npm'
      
      - name: Install dependencies
        run: npm ci
      
      - name: Run linting
        run: npm run lint
      
      - name: Run unit tests
        run: npm run test:unit -- --coverage
      
      - name: Build app
        run: npm run build
      
      - name: Upload coverage
        uses: codecov/codecov-action@v3
        with:
          files: ./coverage/coverage-final.json
      
      - name: Build Docker image
        uses: docker/build-push-action@v4
        with:
          context: ./frontend
          push: true
          tags: ghcr.io/username/e-commerce-frontend:${{ github.sha }}

  deploy:
    needs: build-and-test
    runs-on: ubuntu-latest
    environment: production
    
    steps:
      - uses: actions/checkout@v3
      
      - name: Deploy to Railway
        env:
          RAILWAY_TOKEN: ${{ secrets.RAILWAY_TOKEN }}
        run: |
          npm install -g @railway/cli
          railway up --service frontend
      
      - name: Run E2E tests
        run: npm run test:e2e
      
      - name: Lighthouse CI
        uses: treosh/lighthouse-ci-action@v9
        with:
          configPath: './lighthouse-config.json'
          uploadArtifacts: true
```

---

## 12. Resilience & Fault Tolerance

### 🛡️ **A. Error Boundaries**

```tsx
// File: components/ErrorBoundary.tsx
interface Props {
  children: React.ReactNode;
}

interface State {
  hasError: boolean;
  error: Error | null;
}

export class ErrorBoundary extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error: Error) {
    return { hasError: true, error };
  }

  componentDidCatch(error: Error, info: React.ErrorInfo) {
    // Log to error tracking service
    Sentry.captureException(error, {
      contexts: { react: { componentStack: info.componentStack } },
    });
  }

  render() {
    if (this.state.hasError) {
      return (
        <div className="error-page">
          <h1>Oops! Something went wrong</h1>
          <p>{this.state.error?.message}</p>
          <button onClick={() => window.location.href = '/'}>
            Go Home
          </button>
        </div>
      );
    }

    return this.props.children;
  }
}

// Usage:
<ErrorBoundary>
  <App />
</ErrorBoundary>
```

---

### 🔄 **B. Retry Logic with Exponential Backoff**

```tsx
// File: utils/retry.ts
export const retryWithBackoff = async <T,>(
  fn: () => Promise<T>,
  maxRetries: number = 3,
  baseDelay: number = 1000
): Promise<T> => {
  for (let i = 0; i < maxRetries; i++) {
    try {
      return await fn();
    } catch (error) {
      if (i === maxRetries - 1) throw error;
      
      const delay = baseDelay * Math.pow(2, i); // Exponential backoff
      const jitter = Math.random() * 1000; // Random jitter
      await new Promise(resolve => 
        setTimeout(resolve, delay + jitter)
      );
    }
  }
  throw new Error('Max retries exceeded');
};

// Usage:
const fetchProduct = async (productId) => {
  return retryWithBackoff(
    () => fetch(`/api/products/${productId}`),
    3,
    1000
  );
};

// Server error: First attempt fails
// Wait 1000ms + jitter
// Second attempt fails
// Wait 2000ms + jitter
// Third attempt succeeds ✅
```

---

### 🌐 **C. Network Error Handling**

```tsx
// File: hooks/useNetworkError.ts
export const useNetworkError = () => {
  const [isOnline, setIsOnline] = useState(navigator.onLine);
  
  useEffect(() => {
    const handleOnline = () => {
      setIsOnline(true);
      // Retry failed requests
      eventBus.emit('network:restored');
    };
    
    const handleOffline = () => {
      setIsOnline(false);
      // Show offline indicator
    };
    
    window.addEventListener('online', handleOnline);
    window.addEventListener('offline', handleOffline);
    
    return () => {
      window.removeEventListener('online', handleOnline);
      window.removeEventListener('offline', handleOffline);
    };
  }, []);
  
  return { isOnline };
};

// Usage:
const { isOnline } = useNetworkError();

return (
  <div>
    {!isOnline && (
      <div className="offline-banner">
        You are offline. Some features may not work.
      </div>
    )}
  </div>
);
```

---

## 13. Data Integrity & Synchronization

### ✅ **A. Optimistic Updates**

```tsx
// ❌ BAD: Wait for server response
const handleAddToCart = async (productId) => {
  setLoading(true);
  try {
    const response = await cartService.addItem(productId);
    setCart(response.data);
  } finally {
    setLoading(false);
  }
  // User sees loading spinner for 500ms while request completes
};

// ✅ GOOD: Update UI immediately
const handleAddToCart = async (productId) => {
  // 1. Update local state immediately
  const optimisticCart = {
    ...cart,
    items: [...cart.items, { id: productId, quantity: 1 }],
  };
  setCart(optimisticCart);
  
  try {
    // 2. Make server request
    const response = await cartService.addItem(productId);
    
    // 3. Sync with server response (in case of conflicts)
    setCart(response.data);
  } catch (error) {
    // 4. Rollback on error
    setCart(previousCart);
    showError('Failed to add to cart');
  }
};

// User experience:
// - Item appears in cart immediately (no loading spinner)
// - Server syncs in background
// - If error, quietly rollback and show error
```

---

### 🔄 **B. Form State Synchronization**

```tsx
// ❌ BAD: Save after every keystroke (too many requests)
const [name, setName] = useState('');

useEffect(() => {
  // Save on every keystroke!
  userService.updateProfile({ name });
}, [name]);

// ✅ GOOD: Debounce + Save
const useForm = <T extends Record<string, any>>(
  initialValues: T,
  onSave: (values: T) => Promise<void>
) => {
  const [values, setValues] = useState(initialValues);
  const [isDirty, setIsDirty] = useState(false);
  const debouncedSaveRef = useRef<NodeJS.Timeout>();
  
  const handleChange = (field: string, value: any) => {
    setValues(prev => ({ ...prev, [field]: value }));
    setIsDirty(true);
    
    // Clear existing timeout
    clearTimeout(debouncedSaveRef.current);
    
    // Debounce save: wait 1 second of inactivity
    debouncedSaveRef.current = setTimeout(() => {
      onSave(values);
      setIsDirty(false);
    }, 1000);
  };
  
  return { values, isDirty, handleChange };
};

// Usage:
const form = useForm(
  { name: 'John' },
  async (values) => {
    await userService.updateProfile(values);
  }
);

// Result:
// User types "John Smith" (5 seconds)
// Only 1 save request sent (after inactivity)
```

---

## 14. Implementation Status

### ✅ **Already Implemented in Your Backend**

- Redux/State Management architecture
- Custom hooks for logic reuse
- Service layer for API calls
- Error handling and logging
- Docker containerization
- GitHub Actions CI/CD
- Railway deployment

### 📝 **For Frontend, You'll Need to Implement**

| Area | What to Build | Effort | Priority |
|------|---------------|--------|----------|
| **Architecture** | Component structure, folder organization | Low | 🔴 HIGH |
| **Security** | Input validation, XSS prevention, CSRF | Medium | 🔴 HIGH |
| **State Management** | Redux setup, slices, selectors | Medium | 🔴 HIGH |
| **Performance** | Code splitting, lazy loading, caching | Medium | 🟡 MEDIUM |
| **Testing** | Unit + Integration tests, E2E | High | 🟡 MEDIUM |
| **Observability** | Error tracking (Sentry), Analytics | Medium | 🟡 MEDIUM |
| **Deployment** | Docker, GitHub CI/CD, Railway | Low | 🔴 HIGH |
| **Resilience** | Error boundaries, retry logic, offline | Medium | 🟡 MEDIUM |

---

## 15. Senior Engineer Study Plan

### 📚 **Phase 1: Fundamentals (Week 1-2)**

```markdown
Day 1-2: Architecture & Component Design
- Read section 1 & 2 (Architecture, Component-Driven)
- Design your app folder structure
- Create 5 reusable components (Button, Card, Modal, etc)

Day 3-4: State Management
- Read section 3 (State Management Patterns)
- Learn Redux or Zustand
- Set up global store for auth + products

Day 5-7: SOLID & Design Patterns
- Read section 4 & 5 (SOLID, Design Patterns)
- Refactor existing code to follow SOLID
- Implement 3 design patterns (Custom Hooks, HOC, Observer)

Review: Build a simple product listing page with proper architecture
```

---

### 🔒 **Phase 2: Security & Stability (Week 3)**

```markdown
Day 1-2: Frontend Security
- Read section 6 (Frontend Security)
- Implement input validation + sanitization
- Add CSRF protection if needed

Day 3-4: Error Handling & Resilience
- Read section 11 (Resilience & Fault Tolerance)
- Add Error Boundaries to app
- Implement retry logic + offline handling

Day 5: Data Integrity
- Read section 12 (Data Integrity)
- Implement optimistic updates
- Add form state synchronization

Review: Build login + checkout with proper security
```

---

### ⚡ **Phase 3: Performance (Week 4)**

```markdown
Day 1-2: Bundle Size & Caching
- Read section 7 (Performance)
- Implement code splitting for pages
- Set up service worker caching

Day 3: Image & Asset Optimization
- Optimize images (WebP, responsive srcset)
- Lazy load non-critical images
- Minify CSS/JS

Day 4: Render Performance
- Profile component renders
- Add memoization where needed
- Virtual scrolling for large lists

Day 5: Core Web Vitals
- Run Lighthouse audit
- Optimize LCP, FID, CLS
- Set performance budgets

Review: Optimize your app to Lighthouse >90 score
```

---

### 🧪 **Phase 4: Testing & Observability (Week 5)**

```markdown
Day 1-2: Unit & Integration Testing
- Read section 8 (Testing)
- Write unit tests for utilities
- Write component tests

Day 3: E2E Testing
- Learn Cypress
- Write 5 critical user journeys

Day 4: Observability
- Read section 9 (Observability)
- Set up error tracking (Sentry)
- Add custom analytics

Day 5: Monitoring
- Set up performance monitoring
- Create alerts for errors
- Build monitoring dashboard

Review: 80%+ test coverage, error tracking working
```

---

### 🚀 **Phase 5: Deployment (Week 6)**

```markdown
Day 1: Docker
- Read section 10 (Deployment)
- Create Dockerfile for React app
- Test Docker locally

Day 2-3: CI/CD Pipeline
- Set up GitHub Actions
- Add lint + test + build steps
- Auto-deploy to Railway

Day 4: Monitoring & Alerts
- Set up performance monitoring
- Create error alerts
- Set up status page

Day 5: Documentation
- Document architecture
- Create deployment runbook
- Write onboarding guide

Review: Push code → Tests run → Deploy automatically
```

---

### 🎯 **Resources to Study**

```
Must Read:
- React Docs: https://react.dev
- TypeScript Handbook: https://www.typescriptlang.org
- Redux Official: https://redux.js.org
- Testing Library: https://testing-library.com

Security:
- OWASP Top 10: https://owasp.org/Top10/
- Web Security Academy: https://portswigger.net/web-security

Performance:
- Web Vitals: https://web.dev/vitals/
- Lighthouse: https://developer.chrome.com/docs/lighthouse

Architecture:
- Patterns: https://refactoring.guru/design-patterns
- React Patterns: https://www.patterns.dev
```

---

### ✅ **Checklist: When You're Senior Frontend**

```
Architecture:
☐ Can explain why folder structure matters
☐ Can refactor messy code into proper layers
☐ Can recommend patterns for new features

Security:
☐ Can identify XSS/CSRF vulnerabilities
☐ Can explain secure authentication flow
☐ Can implement security headers

Performance:
☐ Can profile and find bottlenecks
☐ Can optimize bundle size by 30%+
☐ Can explain Core Web Vitals
☐ Can identify N+1 query problems

Testing:
☐ Can achieve 80%+ coverage
☐ Can write meaningful E2E tests
☐ Can mock external dependencies

Observability:
☐ Can set up error tracking
☐ Can create meaningful alerts
☐ Can analyze performance metrics

Deployment:
☐ Can containerize React app
☐ Can set up CI/CD pipeline
☐ Can deploy with zero downtime
☐ Can rollback on errors

Leadership:
☐ Can mentor junior developers
☐ Can make architectural decisions
☐ Can communicate tradeoffs
☐ Can estimate complexity
```

---

## 📝 Notes

- This guide assumes you're building with **React + TypeScript**
- Adapt patterns for Vue/Angular as needed
- Always measure before optimizing
- Security and Testing > Performance Optimization
- Document your decisions and trade-offs

🚀 **Good luck becoming a Senior Frontend Engineer!**
