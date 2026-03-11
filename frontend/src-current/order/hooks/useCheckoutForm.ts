/**
 * useCheckoutForm Hook
 * Form management for checkout with validation
 */

import { useCallback } from 'react';
import { useForm } from '@shared/hooks/useForm';
import {
  validateEmail,
  validatePhoneNumber,
  validateRequired,
  validateMinLength,
  validateCreditCard,
  validateZipCode,
} from '@shared/utils/validators';
import type { ShippingAddress } from '../types/index';

/**
 * Checkout form data
 */
export interface CheckoutFormData {
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  street: string;
  city: string;
  state: string;
  zipCode: string;
  country: string;
  paymentMethod: 'CREDIT_CARD' | 'DEBIT_CARD' | 'PAYPAL' | 'WALLET';
  cardNumber?: string;
  cardName?: string;
  cardExpiry?: string;
  cardCvv?: string;
  notes?: string;
}

/**
 * Initial checkout form state
 */
const initialCheckoutData: CheckoutFormData = {
  firstName: '',
  lastName: '',
  email: '',
  phone: '',
  street: '',
  city: '',
  state: '',
  zipCode: '',
  country: '',
  paymentMethod: 'CREDIT_CARD',
  cardNumber: '',
  cardName: '',
  cardExpiry: '',
  cardCvv: '',
  notes: '',
};

/**
 * useCheckoutForm hook - Manages entire checkout form with validation
 */
export const useCheckoutForm = (
  onSubmit?: (data: CheckoutFormData) => Promise<void> | void
) => {
  /**
   * Validate checkout form
   */
  const validateCheckoutForm = useCallback(
    (values: CheckoutFormData) => {
      const errors: Partial<Record<keyof CheckoutFormData, string>> = {};

      // Shipping address validation
      if (!validateRequired(values.firstName)) {
        errors.firstName = 'First name is required';
      }
      if (!validateRequired(values.lastName)) {
        errors.lastName = 'Last name is required';
      }
      if (!validateRequired(values.email)) {
        errors.email = 'Email is required';
      } else if (!validateEmail(values.email)) {
        errors.email = 'Invalid email address';
      }
      if (!validateRequired(values.phone)) {
        errors.phone = 'Phone is required';
      } else if (!validatePhoneNumber(values.phone)) {
        errors.phone = 'Invalid phone number';
      }
      if (!validateRequired(values.street)) {
        errors.street = 'Street address is required';
      }
      if (!validateRequired(values.city)) {
        errors.city = 'City is required';
      }
      if (!validateRequired(values.state)) {
        errors.state = 'State is required';
      }
      if (!validateRequired(values.zipCode)) {
        errors.zipCode = 'ZIP code is required';
      } else if (!validateZipCode(values.zipCode)) {
        errors.zipCode = 'Invalid ZIP code format';
      }
      if (!validateRequired(values.country)) {
        errors.country = 'Country is required';
      }

      // Payment validation
      if (!validateRequired(values.paymentMethod)) {
        errors.paymentMethod = 'Payment method is required';
      }

      // Card payment validation (if card selected)
      if (values.paymentMethod === 'CREDIT_CARD' || values.paymentMethod === 'DEBIT_CARD') {
        if (!validateRequired(values.cardNumber)) {
          errors.cardNumber = 'Card number is required';
        } else if (values.cardNumber && !validateCreditCard(values.cardNumber)) {
          errors.cardNumber = 'Invalid card number';
        }
        if (!validateRequired(values.cardName)) {
          errors.cardName = 'Cardholder name is required';
        }
        if (!validateRequired(values.cardExpiry)) {
          errors.cardExpiry = 'Card expiry is required';
        }
        if (!validateRequired(values.cardCvv)) {
          errors.cardCvv = 'CVV is required';
        } else if (!validateMinLength(values.cardCvv, 3)) {
          errors.cardCvv = 'Invalid CVV';
        }
      }

      return errors;
    },
    []
  );

  const form = useForm<CheckoutFormData>({
    initialValues: initialCheckoutData,
    validate: validateCheckoutForm,
    onSubmit: async (values) => {
      if (onSubmit) {
        await onSubmit(values);
      }
    },
  });

  /**
   * Get shipping address from form values
   */
  const getShippingAddress = (): ShippingAddress => ({
    firstName: form.values.firstName,
    lastName: form.values.lastName,
    email: form.values.email,
    phone: form.values.phone,
    street: form.values.street,
    city: form.values.city,
    state: form.values.state,
    zipCode: form.values.zipCode,
    country: form.values.country,
  });

  /**
   * Check if card payment is selected
   */
  const isCardPayment =
    form.values.paymentMethod === 'CREDIT_CARD' || form.values.paymentMethod === 'DEBIT_CARD';

  /**
   * Check if all required fields are filled
   */
  const isShippingAddressValid =
    form.values.firstName &&
    form.values.lastName &&
    form.values.email &&
    form.values.phone &&
    form.values.street &&
    form.values.city &&
    form.values.state &&
    form.values.zipCode &&
    form.values.country &&
    !form.errors.firstName &&
    !form.errors.lastName &&
    !form.errors.email &&
    !form.errors.phone &&
    !form.errors.street &&
    !form.errors.city &&
    !form.errors.state &&
    !form.errors.zipCode &&
    !form.errors.country;

  const isPaymentInfoValid =
    isCardPayment && form.values.cardNumber && form.values.cardName && form.values.cardExpiry && form.values.cardCvv
      ? !form.errors.cardNumber && !form.errors.cardName && !form.errors.cardExpiry && !form.errors.cardCvv
      : true;

  const isFormValid = isShippingAddressValid && isPaymentInfoValid && form.values.paymentMethod;

  return {
    // Form state
    values: form.values,
    errors: form.errors,
    touched: form.touched,
    isDirty: form.isDirty,
    isSubmitting: form.isSubmitting,
    isValid: form.isValid,

    // Form methods
    getFieldProps: form.getFieldProps,
    getFieldMeta: form.getFieldMeta,
    setFieldValue: form.setFieldValue,
    setFieldError: form.setFieldError,
    setFieldTouched: form.setFieldTouched,
    validateField: form.validateField,
    validateForm: form.validateForm,
    resetForm: form.resetForm,
    submitForm: form.submitForm,

    // Checkout-specific methods
    getShippingAddress,
    isCardPayment,
    isShippingAddressValid,
    isPaymentInfoValid,
    isFormValid,
  };
};

/**
 * useShippingAddressForm - Simpler form for just shipping address
 */
export const useShippingAddressForm = () => {
  const validateShippingForm = useCallback(
    (values: Omit<ShippingAddress, 'id' | 'isDefault'>) => {
      const errors: Partial<Record<keyof typeof values, string>> = {};

      if (!validateRequired(values.firstName)) errors.firstName = 'Required';
      if (!validateRequired(values.lastName)) errors.lastName = 'Required';
      if (!validateEmail(values.email)) errors.email = 'Invalid email';
      if (!validatePhoneNumber(values.phone)) errors.phone = 'Invalid phone';
      if (!validateRequired(values.street)) errors.street = 'Required';
      if (!validateRequired(values.city)) errors.city = 'Required';
      if (!validateRequired(values.state)) errors.state = 'Required';
      if (!validateZipCode(values.zipCode)) errors.zipCode = 'Invalid ZIP code';
      if (!validateRequired(values.country)) errors.country = 'Required';

      return errors;
    },
    []
  );

  const form = useForm({
    initialValues: {
      firstName: '',
      lastName: '',
      email: '',
      phone: '',
      street: '',
      city: '',
      state: '',
      zipCode: '',
      country: '',
    },
    validate: validateShippingForm,
    onSubmit: async () => {
      // No-op
    },
  });

  return {
    ...form,
    getAddress: (): ShippingAddress => ({
      ...form.values,
      id: undefined,
      isDefault: false,
    }),
  };
};

/**
 * usePaymentForm - Form for payment details
 */
export const usePaymentForm = () => {
  const validatePaymentForm = useCallback(
    (values: {
      method: 'CREDIT_CARD' | 'DEBIT_CARD' | 'PAYPAL' | 'WALLET';
      cardNumber: string;
      cardName: string;
      cardExpiry: string;
      cardCvv: string;
    }) => {
      const errors: Partial<Record<keyof typeof values, string>> = {};

      if (!validateRequired(values.method)) errors.method = 'Required';
      if (!validateCreditCard(values.cardNumber)) errors.cardNumber = 'Invalid card number';
      if (!validateRequired(values.cardName)) errors.cardName = 'Required';
      if (!validateRequired(values.cardExpiry)) errors.cardExpiry = 'Required';
      if (!validateMinLength(values.cardCvv, 3)) errors.cardCvv = 'Invalid CVV';

      return errors;
    },
    []
  );

  return useForm({
    initialValues: {
      method: 'CREDIT_CARD' as const,
      cardNumber: '',
      cardName: '',
      cardExpiry: '',
      cardCvv: '',
    },
    validate: validatePaymentForm,
    onSubmit: async () => {
      // No-op
    },
  });
};
