import React, { useEffect, useMemo, useState } from 'react';
import type { AxiosError } from 'axios';
import { CheckCircle2, ImagePlus, RefreshCw, XCircle } from 'lucide-react';
import { Button, Card, CardContent, CardDescription, CardHeader, CardTitle, Input } from '@/shared/components/ui';
import productService from '@/features/products/api/productService';
import type { CreateProductPayload, ProductDetail, ProductListItem } from '@/features/products/types';

const getApiErrorMessage = (error: unknown, fallback: string) => {
  const axiosError = error as AxiosError<{ message?: string; error?: string | { message?: string } }>;
  const payload = axiosError?.response?.data;
  return payload?.message ||
    (typeof payload?.error === 'string' ? payload.error : payload?.error?.message) ||
    axiosError?.message ||
    fallback;
};

const formatMoney = (value: number, currency: string) =>
  new Intl.NumberFormat('en-US', { style: 'currency', currency: currency || 'USD' }).format(value);

const ProductsPage: React.FC = () => {
  const [products, setProducts] = useState<ProductListItem[]>([]);
  const [detailsById, setDetailsById] = useState<Record<string, ProductDetail>>({});
  const [selectedProductId, setSelectedProductId] = useState<string | null>(null);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [status, setStatus] = useState<string | null>(null);
  const [busyAction, setBusyAction] = useState<'activate' | 'deactivate' | 'discontinue' | 'upload' | null>(null);
  const [forcedDiscontinuedProductIds, setForcedDiscontinuedProductIds] = useState<string[]>([]);
  const [isCreating, setIsCreating] = useState(false);
  const [skuLookup, setSkuLookup] = useState('');
  const [isSkuLookupBusy, setIsSkuLookupBusy] = useState(false);
  const [uploadedImageUrlsByProduct, setUploadedImageUrlsByProduct] = useState<Record<string, string[]>>({});
  const [imageUrlToDelete, setImageUrlToDelete] = useState('');
  const [createForm, setCreateForm] = useState<CreateProductPayload>({
    sku: '',
    name: '',
    description: '',
    price: 0,
    currencyCode: 'USD',
    initialStock: 0,
  });

  const loadProducts = async () => {
    setLoading(true);
    setError(null);
    try {
      const list = await productService.listProducts();
      setProducts(list);

      const detailResults = await Promise.allSettled(
        list.map(async (item) => {
          const detail = await productService.getProductById(item.id);
          return [item.id, detail] as const;
        })
      );

      const nextDetails: Record<string, ProductDetail> = {};
      detailResults.forEach((result) => {
        if (result.status === 'fulfilled') {
          const [id, detail] = result.value;
          nextDetails[id] = detail;
        }
      });
      setDetailsById(nextDetails);

      if (list.length > 0 && !selectedProductId) {
        setSelectedProductId(list[0].id);
      }
    } catch (e) {
      setError(getApiErrorMessage(e, 'Failed to load products.'));
      setProducts([]);
      setDetailsById({});
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadProducts();
  }, []);

  const selectedProduct = useMemo(
    () => products.find((item) => item.id === selectedProductId) ?? null,
    [products, selectedProductId]
  );

  const selectedDetail = selectedProductId ? detailsById[selectedProductId] : undefined;
  const selectedDetailKnown = Boolean(selectedDetail);
  const selectedIsDiscontinued = Boolean(
    selectedDetail?.discontinued ||
      (selectedProductId ? forcedDiscontinuedProductIds.includes(selectedProductId) : false)
  );
  const canShowRestrictedActions = Boolean(selectedProduct && selectedDetailKnown);

  const toListItem = (detail: ProductDetail): ProductListItem => ({
    id: detail.id,
    sku: detail.sku,
    name: detail.name,
    price: detail.price,
    currency: detail.currency,
    availableStock: detail.availableStock,
    active: detail.active,
  });

  const runAction = async (
    action: 'activate' | 'deactivate' | 'discontinue' | 'upload',
    handler: () => Promise<void>,
    successMessage: string,
    preventWhenDiscontinued = false
  ) => {
    if (preventWhenDiscontinued && selectedIsDiscontinued) {
      setError('This product is discontinued and cannot be modified by backend rules.');
      setStatus(null);
      return;
    }

    setBusyAction(action);
    setError(null);
    setStatus(null);
    try {
      await handler();
      setStatus(successMessage);
      await loadProducts();
    } catch (e) {
      const message = getApiErrorMessage(e, 'Product action failed.');
      const normalizedMessage = message.toLowerCase();
      const looksLikeDiscontinuedConflict =
        normalizedMessage.includes('invalid product state') ||
        normalizedMessage.includes('discontinued') ||
        normalizedMessage.includes('prod_conflict_003');

      if (looksLikeDiscontinuedConflict && selectedProductId) {
        setForcedDiscontinuedProductIds((prev) =>
          prev.includes(selectedProductId) ? prev : [...prev, selectedProductId]
        );
      }

      setError(getApiErrorMessage(e, 'Product action failed.'));
    } finally {
      setBusyAction(null);
    }
  };

  const handleCreateProduct = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError(null);
    setStatus(null);

    if (!createForm.sku.trim() || !createForm.name.trim()) {
      setError('SKU and Name are required.');
      return;
    }

    if (!Number.isFinite(createForm.price) || createForm.price <= 0) {
      setError('Price must be greater than zero.');
      return;
    }

    if (!Number.isFinite(createForm.initialStock) || createForm.initialStock < 0) {
      setError('Initial stock must be zero or greater.');
      return;
    }

    setIsCreating(true);
    try {
      const created = await productService.createProduct({
        sku: createForm.sku.trim(),
        name: createForm.name.trim(),
        description: createForm.description?.trim() || '',
        price: Number(createForm.price),
        currencyCode: (createForm.currencyCode || 'USD').trim().toUpperCase(),
        initialStock: Math.floor(Number(createForm.initialStock)),
      });

      setStatus(`Product created: ${created.name}`);
      setCreateForm({
        sku: '',
        name: '',
        description: '',
        price: 0,
        currencyCode: createForm.currencyCode || 'USD',
        initialStock: 0,
      });
      await loadProducts();
      setSelectedProductId(created.id);
    } catch (e) {
      setError(getApiErrorMessage(e, 'Failed to create product.'));
    } finally {
      setIsCreating(false);
    }
  };

  const handleLookupBySku = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const sku = skuLookup.trim();
    if (!sku) {
      setError('Enter an SKU to lookup product details.');
      return;
    }

    setIsSkuLookupBusy(true);
    setError(null);
    setStatus(null);
    try {
      const detail = await productService.getProductBySku(sku);
      setDetailsById((prev) => ({ ...prev, [detail.id]: detail }));
      setProducts((prev) => {
        if (prev.some((item) => item.id === detail.id)) return prev;
        return [toListItem(detail), ...prev];
      });
      setSelectedProductId(detail.id);
      setStatus(`Loaded product by SKU: ${detail.sku}`);
    } catch (e) {
      setError(getApiErrorMessage(e, 'Failed to fetch product by SKU.'));
    } finally {
      setIsSkuLookupBusy(false);
    }
  };

  return (
    <div className="mx-auto w-full max-w-7xl space-y-6 px-4 py-6 sm:px-6 lg:px-8">
      <div className="rounded-2xl border border-slate-200 bg-gradient-to-r from-slate-50 via-white to-blue-50 p-5 shadow-sm">
        <h2 className="mb-1 text-2xl font-bold tracking-tight text-slate-900">Manage Products</h2>
        <p className="text-sm text-slate-600">Owner product controls with discontinued-state protections.</p>
      </div>

      {status && (
        <div className="rounded-xl border border-emerald-200 bg-emerald-50/80 px-4 py-3 text-sm font-medium text-emerald-700 shadow-sm">
          {status}
        </div>
      )}

      {error && (
        <div className="rounded-xl border border-rose-200 bg-rose-50/80 px-4 py-3 text-sm font-medium text-rose-700 shadow-sm">
          {error}
        </div>
      )}

      <Card className="border-slate-200 shadow-sm">
        <CardHeader>
          <CardTitle className="text-lg text-slate-900">Create Product</CardTitle>
          <CardDescription className="text-slate-600">Admin form mapped to POST /products.</CardDescription>
        </CardHeader>
        <CardContent className="space-y-1">
          <form onSubmit={handleCreateProduct} className="space-y-3">
            <div className="grid gap-3 md:grid-cols-2">
              <Input
                value={createForm.sku}
                onChange={(event) => setCreateForm((prev) => ({ ...prev, sku: event.target.value }))}
                placeholder="SKU"
                required
              />
              <Input
                value={createForm.name}
                onChange={(event) => setCreateForm((prev) => ({ ...prev, name: event.target.value }))}
                placeholder="Product name"
                required
              />
            </div>

            <textarea
              value={createForm.description}
              onChange={(event) => setCreateForm((prev) => ({ ...prev, description: event.target.value }))}
              placeholder="Description"
              rows={3}
              className="w-full rounded-md border border-slate-200 px-3 py-2 text-sm text-slate-800 shadow-sm"
            />

            <div className="grid gap-3 md:grid-cols-3">
              <Input
                type="number"
                min={0.01}
                step={0.01}
                value={createForm.price}
                onChange={(event) => setCreateForm((prev) => ({ ...prev, price: Number(event.target.value) }))}
                placeholder="Price"
                required
              />
              <Input
                value={createForm.currencyCode}
                onChange={(event) => setCreateForm((prev) => ({ ...prev, currencyCode: event.target.value.toUpperCase() }))}
                placeholder="Currency"
                required
              />
              <Input
                type="number"
                min={0}
                step={1}
                value={createForm.initialStock}
                onChange={(event) => setCreateForm((prev) => ({ ...prev, initialStock: Number(event.target.value) }))}
                placeholder="Initial stock"
                required
              />
            </div>

            <Button type="submit" disabled={isCreating} className="w-full md:w-auto md:min-w-40">
              {isCreating ? 'Creating...' : 'Create Product'}
            </Button>
          </form>
        </CardContent>
      </Card>

      <Card className="border-slate-200 shadow-sm">
        <CardHeader>
          <CardTitle className="text-lg text-slate-900">Lookup Product by SKU</CardTitle>
          <CardDescription className="text-slate-600">Quick check mapped to GET /products?sku=...</CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleLookupBySku} className="flex flex-col gap-3 md:flex-row">
            <Input
              value={skuLookup}
              onChange={(event) => setSkuLookup(event.target.value)}
              placeholder="Enter SKU"
              required
            />
            <Button type="submit" disabled={isSkuLookupBusy} className="md:w-auto md:min-w-36">
              {isSkuLookupBusy ? 'Looking up...' : 'Find by SKU'}
            </Button>
          </form>
        </CardContent>
      </Card>

      <div className="grid gap-6 lg:grid-cols-[1.2fr_1fr]">
        <Card className="border-slate-200 shadow-sm">
          <CardHeader>
            <CardTitle className="text-lg text-slate-900">Products</CardTitle>
            <CardDescription className="text-slate-600">Select a product to manage state and images.</CardDescription>
          </CardHeader>
          <CardContent>
            {loading ? (
              <p className="text-sm text-gray-600">Loading products...</p>
            ) : products.length === 0 ? (
              <p className="text-sm text-gray-600">No products available.</p>
            ) : (
              <div className="space-y-2">
                {products.map((item) => {
                  const detail = detailsById[item.id];
                  const isSelected = item.id === selectedProductId;
                  const isDiscontinued = Boolean(detail?.discontinued);
                  return (
                    <button
                      key={item.id}
                      onClick={() => setSelectedProductId(item.id)}
                      className={`w-full rounded-lg border px-3 py-3 text-left transition ${
                        isSelected
                          ? 'border-blue-300 bg-blue-50 shadow-sm'
                          : 'border-slate-200 bg-white hover:border-slate-300 hover:shadow-sm'
                      }`}
                    >
                      <div className="flex items-center justify-between gap-3">
                        <div>
                          <p className="font-semibold text-gray-900">{item.name}</p>
                          <p className="text-xs text-gray-500">SKU: {item.sku}</p>
                        </div>
                        <div className="text-right">
                          <p className="text-sm font-semibold text-gray-800">{formatMoney(item.price, item.currency)}</p>
                          <p className="text-xs text-gray-500">Stock: {item.availableStock}</p>
                        </div>
                      </div>
                      <div className="mt-2 flex items-center gap-2">
                        <span className={`inline-flex rounded-full border px-2 py-0.5 text-[11px] font-semibold ${item.active ? 'border-emerald-200 bg-emerald-50 text-emerald-700' : 'border-amber-200 bg-amber-50 text-amber-700'}`}>
                          {item.active ? 'Active' : 'Inactive'}
                        </span>
                        {isDiscontinued && (
                          <span className="inline-flex rounded-full border border-rose-200 bg-rose-50 px-2 py-0.5 text-[11px] font-semibold text-rose-700">
                            Discontinued
                          </span>
                        )}
                      </div>
                    </button>
                  );
                })}
              </div>
            )}
          </CardContent>
        </Card>

        <Card className="border-slate-200 shadow-sm">
          <CardHeader>
            <CardTitle className="text-lg text-slate-900">Owner Actions</CardTitle>
            <CardDescription className="text-slate-600">
              {selectedProduct ? `Selected: ${selectedProduct.name}` : 'Select a product from the list.'}
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            {selectedIsDiscontinued && (
              <div className="rounded-md border border-rose-200 bg-rose-50 px-3 py-2 text-xs text-rose-700">
                Discontinued product: modify actions are blocked (activate/deactivate/price/details/images).
              </div>
            )}

            <div className={`grid gap-2 ${canShowRestrictedActions ? 'grid-cols-3' : 'grid-cols-2'}`}>
              {canShowRestrictedActions && (
                <Button
                  disabled={!selectedProduct || busyAction !== null || selectedIsDiscontinued}
                  title={selectedIsDiscontinued ? 'Discontinued products cannot be activated by backend rules.' : undefined}
                  onClick={() =>
                    void runAction('activate', async () => {
                      if (!selectedProduct) return;
                      await productService.activateProduct(selectedProduct.id);
                    }, 'Product activated.', true)
                  }
                  className="w-full"
                >
                  <CheckCircle2 className="mr-1 h-4 w-4" /> Activate
                </Button>
              )}

              <Button
                variant="outline"
                disabled={!selectedProduct || busyAction !== null || selectedIsDiscontinued}
                title={selectedIsDiscontinued ? 'Discontinued products cannot be deactivated by backend rules.' : undefined}
                onClick={() =>
                  void runAction('deactivate', async () => {
                    if (!selectedProduct) return;
                    await productService.deactivateProduct(selectedProduct.id);
                  }, 'Product deactivated.', true)
                }
                className="w-full"
              >
                <XCircle className="mr-1 h-4 w-4" /> Deactivate
              </Button>

              <Button
                variant="outline"
                disabled={!selectedProduct || busyAction !== null || selectedIsDiscontinued}
                title={selectedIsDiscontinued ? 'Discontinued products cannot be discontinued again.' : undefined}
                onClick={() =>
                  void runAction('discontinue', async () => {
                    if (!selectedProduct) return;
                    await productService.discontinueProduct(selectedProduct.id);
                  }, 'Product discontinued.', true)
                }
                className="w-full"
              >
                <XCircle className="mr-1 h-4 w-4" /> Discontinue
              </Button>
            </div>

            {canShowRestrictedActions && (
              <div className="space-y-2 rounded-lg border border-gray-200 bg-gray-50 p-3">
                <p className="text-xs font-semibold uppercase tracking-wide text-slate-600">Product image</p>
                <Input
                  type="file"
                  accept="image/*"
                  disabled={!selectedProduct || busyAction !== null || selectedIsDiscontinued}
                  onChange={(event) => {
                    const file = event.target.files?.[0] ?? null;
                    setSelectedFile(file);
                  }}
                />
                <Button
                  disabled={!selectedProduct || !selectedFile || busyAction !== null || selectedIsDiscontinued}
                  title={selectedIsDiscontinued ? 'Image upload is blocked for discontinued products.' : undefined}
                  onClick={() =>
                    void runAction('upload', async () => {
                      if (!selectedProduct || !selectedFile) return;
                      const uploaded = await productService.uploadProductImage(selectedProduct.id, selectedFile);
                      setUploadedImageUrlsByProduct((prev) => {
                        const current = prev[selectedProduct.id] ?? [];
                        if (current.includes(uploaded.imageUrl)) return prev;
                        return { ...prev, [selectedProduct.id]: [...current, uploaded.imageUrl] };
                      });
                      setSelectedFile(null);
                    }, 'Image uploaded successfully.', true)
                  }
                  className="w-full"
                >
                  <ImagePlus className="mr-2 h-4 w-4" /> Upload image
                </Button>

                <Input
                  value={imageUrlToDelete}
                  onChange={(event) => setImageUrlToDelete(event.target.value)}
                  placeholder="Image URL to delete"
                  disabled={!selectedProduct || busyAction !== null || selectedIsDiscontinued}
                />
                <Button
                  variant="outline"
                  disabled={!selectedProduct || !imageUrlToDelete.trim() || busyAction !== null || selectedIsDiscontinued}
                  title={selectedIsDiscontinued ? 'Image delete is blocked for discontinued products.' : undefined}
                  onClick={() =>
                    void runAction('upload', async () => {
                      if (!selectedProduct) return;
                      await productService.deleteProductImage(selectedProduct.id, imageUrlToDelete.trim());
                      setUploadedImageUrlsByProduct((prev) => ({
                        ...prev,
                        [selectedProduct.id]: (prev[selectedProduct.id] ?? []).filter((url) => url !== imageUrlToDelete.trim()),
                      }));
                      setImageUrlToDelete('');
                    }, 'Image deleted successfully.', true)
                  }
                  className="w-full"
                >
                  Delete image by URL
                </Button>

                {selectedProduct && (uploadedImageUrlsByProduct[selectedProduct.id] ?? []).length > 0 && (
                  <div className="rounded-md border border-slate-200 bg-white p-2 shadow-sm">
                    <p className="mb-1 text-[11px] font-semibold uppercase tracking-wide text-gray-500">Uploaded image URLs</p>
                    <div className="space-y-1">
                      {(uploadedImageUrlsByProduct[selectedProduct.id] ?? []).map((url) => (
                        <button
                          key={url}
                          onClick={() => setImageUrlToDelete(url)}
                          className="w-full truncate rounded border border-slate-200 px-2 py-1 text-left text-xs text-slate-700 hover:bg-slate-50"
                          title={url}
                        >
                          {url}
                        </button>
                      ))}
                    </div>
                  </div>
                )}

                {selectedIsDiscontinued && (
                  <p className="text-xs text-amber-700">
                    Actions are disabled because this product is discontinued and backend blocks all modify operations.
                  </p>
                )}
              </div>
            )}

            {!selectedDetailKnown && selectedProduct && (
              <p className="text-xs text-gray-500">Loading product state...</p>
            )}

            <Button
              variant="outline"
              disabled={loading}
              onClick={() => void loadProducts()}
              className="w-full"
            >
              <RefreshCw className="mr-2 h-4 w-4" /> Refresh products
            </Button>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};

export default ProductsPage;
