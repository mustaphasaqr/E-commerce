import React, { useEffect, useMemo, useState } from 'react';
import type { AxiosError } from 'axios';
import { CheckCircle2, ImagePlus, RefreshCw, XCircle } from 'lucide-react';
import { Button, Card, CardContent, CardDescription, CardHeader, CardTitle, Input } from '@/shared/components/ui';
import productService from '@/features/products/api/productService';
import type { ProductDetail, ProductListItem } from '@/features/products/types';

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
  const canShowRestrictedActions = Boolean(selectedProduct && selectedDetailKnown && !selectedIsDiscontinued);

  const runAction = async (
    action: 'activate' | 'deactivate' | 'discontinue' | 'upload',
    handler: () => Promise<void>,
    successMessage: string
  ) => {
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

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold mb-1">Manage Products</h2>
        <p className="text-sm text-gray-600">Owner product controls with discontinued-state protections.</p>
      </div>

      {status && (
        <div className="rounded-lg border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-700">
          {status}
        </div>
      )}

      {error && (
        <div className="rounded-lg border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700">
          {error}
        </div>
      )}

      <div className="grid gap-6 lg:grid-cols-[1.2fr_1fr]">
        <Card className="border-gray-200">
          <CardHeader>
            <CardTitle className="text-lg">Products</CardTitle>
            <CardDescription>Select a product to manage state and images.</CardDescription>
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
                          ? 'border-blue-300 bg-blue-50'
                          : 'border-gray-200 bg-white hover:border-gray-300'
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

        <Card className="border-gray-200">
          <CardHeader>
            <CardTitle className="text-lg">Owner Actions</CardTitle>
            <CardDescription>
              {selectedProduct ? `Selected: ${selectedProduct.name}` : 'Select a product from the list.'}
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            {selectedIsDiscontinued && (
              <div className="rounded-md border border-rose-200 bg-rose-50 px-3 py-2 text-xs text-rose-700">
                Discontinued product: Activate and Upload image are disabled and will return conflict.
              </div>
            )}

            <div className={`grid gap-2 ${canShowRestrictedActions ? 'grid-cols-3' : 'grid-cols-2'}`}>
              {canShowRestrictedActions && (
                <Button
                  disabled={!selectedProduct || busyAction !== null}
                  onClick={() =>
                    void runAction('activate', async () => {
                      if (!selectedProduct) return;
                      await productService.activateProduct(selectedProduct.id);
                    }, 'Product activated.')
                  }
                  className="w-full"
                >
                  <CheckCircle2 className="mr-1 h-4 w-4" /> Activate
                </Button>
              )}

              <Button
                variant="outline"
                disabled={!selectedProduct || busyAction !== null}
                onClick={() =>
                  void runAction('deactivate', async () => {
                    if (!selectedProduct) return;
                    await productService.deactivateProduct(selectedProduct.id);
                  }, 'Product deactivated.')
                }
                className="w-full"
              >
                <XCircle className="mr-1 h-4 w-4" /> Deactivate
              </Button>

              <Button
                variant="outline"
                disabled={!selectedProduct || busyAction !== null}
                onClick={() =>
                  void runAction('discontinue', async () => {
                    if (!selectedProduct) return;
                    await productService.discontinueProduct(selectedProduct.id);
                  }, 'Product discontinued.')
                }
                className="w-full"
              >
                <XCircle className="mr-1 h-4 w-4" /> Discontinue
              </Button>
            </div>

            {canShowRestrictedActions && (
              <div className="space-y-2 rounded-lg border border-gray-200 bg-gray-50 p-3">
                <p className="text-xs font-semibold uppercase tracking-wide text-gray-600">Product image</p>
                <Input
                  type="file"
                  accept="image/*"
                  disabled={!selectedProduct || busyAction !== null}
                  onChange={(event) => {
                    const file = event.target.files?.[0] ?? null;
                    setSelectedFile(file);
                  }}
                />
                <Button
                  disabled={!selectedProduct || !selectedFile || busyAction !== null}
                  onClick={() =>
                    void runAction('upload', async () => {
                      if (!selectedProduct || !selectedFile) return;
                      await productService.uploadProductImage(selectedProduct.id, selectedFile);
                      setSelectedFile(null);
                    }, 'Image uploaded successfully.')
                  }
                  className="w-full"
                >
                  <ImagePlus className="mr-2 h-4 w-4" /> Upload image
                </Button>
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
