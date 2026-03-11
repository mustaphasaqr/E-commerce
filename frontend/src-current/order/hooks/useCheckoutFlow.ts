/**
 * useCheckoutFlow Hook
 * Combines checkout form with order API for complete checkout workflow
 */

import { useCallback } from 'react';
import { useOrder } from './useOrder';
import { useCheckoutForm } from './useCheckoutForm';

/**
 * useCheckoutFlow hook - Complete checkout workflow
 */
export const useCheckoutFlow = () => {
  const { checkout, isCheckingOut, error } = useOrder();
  const checkoutForm = useCheckoutForm();

  /**
   * Handle checkout submission
   */
  const handleCheckoutSubmit = useCallback(
    async (e: React.FormEvent) => {
      e.preventDefault();

      if (!checkoutForm.isFormValid) {
        return;
      }

      try {
        const shippingAddress = checkoutForm.getShippingAddress();

        await checkout({
          shippingAddress,
          paymentMethod: checkoutForm.values.paymentMethod as any,
          paymentDetails: checkoutForm.isCardPayment
            ? {
                cardNumber: checkoutForm.values.cardNumber,
                cardName: checkoutForm.values.cardName,
                cardExpiry: checkoutForm.values.cardExpiry,
                cardCvv: checkoutForm.values.cardCvv,
              }
            : undefined,
          notes: checkoutForm.values.notes,
        });
      } catch (err) {
        console.error('Checkout failed:', err);
      }
    },
    [checkout, checkoutForm]
  );

  /**
   * Validate and move to next step
   */
  const validateStep = useCallback(
    (step: 'shipping' | 'payment' | 'review') => {
      switch (step) {
        case 'shipping':
          return checkoutForm.isShippingAddressValid;
        case 'payment':
          return checkoutForm.isPaymentInfoValid;
        case 'review':
          return checkoutForm.isFormValid;
        default:
          return false;
      }
    },
    [checkoutForm]
  );

  return {
    // Form management
    form: checkoutForm,
    handleCheckoutSubmit,
    validateStep,

    // Checkout state
    isCheckingOut,
    checkoutError: error,

    // Methods
    submitCheckout: handleCheckoutSubmit,
  };
};
