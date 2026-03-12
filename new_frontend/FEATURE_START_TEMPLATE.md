1-
- [ ] **01.Contract** - Define interface matching backend response
- [ ] **02.Service** - Create API service with Promise<Type>
- [ ] **03.Test-Service** - Write mirror test matching backend test
- [ ] **04.Zod** - Validation schema for user inputs
- [ ] **05.Store** - Zustand store if feature has global state
- [ ] **06.Hook** - Custom hook wrapping service + store
- [ ] **07.Render** - Component JSX displaying data
- [ ] **08.Interact** - onClick, onChange handlers
- [ ] **09.Local-State** - useState for form inputs
- [ ] **10.Form-Validation** - React Hook Form + Zod integration
- [ ] **11.Routing** - Routes defined in AppRoutes.tsx
- [ ] **12.Styling** - Tailwind responsive design
- [ ] **13.Loading** - Spinner shown while loading
- [ ] **14.A11y** - ARIA labels, keyboard nav, alt text
- [ ] **15.Logging** - Axios logs 📤✅❌ visible in console

2-

STEP 1: Define Test Formula
        └─ Pattern: async service → await call → Promise<Type> return

STEP 2: Write Test Using Formula
        └─ Test structure IS the pattern
        └─ await productService.getProducts() → Promise<Product[]>

STEP 3: Implement Service Same Formula
        └─ Service MUST match test's exact pattern
        └─ async getProducts(): Promise<Product[]>

STEP 4: Build Hook Using Test Formula
        └─ Hook calls service exactly like test does
        └─ useState<Product[]> matches test expectation
        └─ useEffect fetches same way test calls

STEP 5: Build Component Using Test Formula
        └─ Component receives Product[] from hook
        └─ Maps/displays same way test uses data

RESULT: Test passed → Code uses same formula → Code guaranteed to work


3-shadcn/ui + Tailwind CSS + Lucide Icons + Radix UI primitives + Preline UI (Layouts / Pages / Sections)

4- do not change or touch anything in backend 

5- "feature has "number" APIs in backend 

6- screenshot 

**Mark which ones apply. Skip the rest.**

---

### Step 1: Define Contract (Types First)

**Backend test reference:** Look at `src/test/java/.../[Feature]ServiceTest.java`

**What backend test expects:**
```
[Paste the expected shape here]
```

**Frontend interface created:**
```typescript
// src/features/[feature]/types/index.ts
[Paste code here]
```

---

### Step 2: Write Service Test (Mirror Backend)

**Backend test structure:**
```java
[Copy structure from backend test]
```

**Frontend service test created:**
```typescript
// src/features/[feature]/api/[feature]Service.test.ts
[Paste code here]
```

**Test result:** ✅ PASS / ❌ FAIL

---

### Step 3: Implement Service

**Service implementation:**
```typescript
// src/features/[feature]/api/[feature]Service.ts
[Paste code here]
```

**Axios logs visible:**
```
📤 API Request: [method] [endpoint]
✅ API Success: 200 [endpoint]
```

---

### Step 4: Build Hook

**Hook implementation:**
```typescript
// src/features/[feature]/hooks/use[Feature].ts
[Paste code here]
```

---

### Step 5: Build Component

**Component implementation:**
```typescript
// src/features/[feature]/components/[Feature]List.tsx
[Paste code here]
```

---

### Pre-Completion Checklist

Before marking feature "done":

- [ ] Ran `npm run lint` (no errors)
- [ ] Ran `npm run type-check` (no TypeScript errors)
- [ ] Ran `npm run test` (service test passes)
- [ ] Opened DevTools Console (📤✅❌ logs visible)
- [ ] Checked Network tab (no 404s or 500s)
- [ ] Tested with keyboard only (accessibility works)
- [ ] Checked all 15 concerns above (marked which ones done)

---

**Status:** [NOT STARTED / IN PROGRESS / BLOCKED / COMPLETE]

**Blockers:** [List any issues]

**Next Step:** [What's next?]
