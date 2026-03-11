/**
 * useForm Hook
 * Custom hook for managing form state with validation
 */

import { useState, useCallback, useRef } from 'react';
import { NormalizedError } from '@shared/utils/errorHandler';

/**
 * Form field state
 */
export interface FormField<T> {
  value: T;
  touched: boolean;
  error?: string;
}

/**
 * Form state
 */
export interface FormState<T> {
  values: T;
  touched: Partial<Record<keyof T, boolean>>;
  errors: Partial<Record<keyof T, string>>;
  isSubmitting: boolean;
  isDirty: boolean;
}

/**
 * Form validator
 */
export type FormValidator<T> = (
  values: T
) => Partial<Record<keyof T, string>> | Promise<Partial<Record<keyof T, string>>>;

/**
 * Form submit handler
 */
export type FormSubmitHandler<T> = (
  values: T
) => void | Promise<void>;

/**
 * useForm options
 */
export interface UseFormOptions<T> {
  initialValues: T;
  validate?: FormValidator<T>;
  onSubmit: FormSubmitHandler<T>;
  onSuccess?: () => void;
  onError?: (error: NormalizedError | Error) => void;
}

/**
 * useForm result
 */
export interface UseFormResult<T> {
  values: T;
  touched: Partial<Record<keyof T, boolean>>;
  errors: Partial<Record<keyof T, string>>;
  isSubmitting: boolean;
  isDirty: boolean;
  isValid: boolean;
  getFieldProps: (name: keyof T) => {
    name: string;
    value: any;
    onChange: (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => void;
    onBlur: (e: React.FocusEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => void;
  };
  getFieldMeta: (name: keyof T) => {
    value: any;
    error?: string;
    touched: boolean;
  };
  setFieldValue: (name: keyof T, value: any) => void;
  setFieldError: (name: keyof T, error: string) => void;
  setFieldTouched: (name: keyof T, touched: boolean) => void;
  setValues: (values: Partial<T>) => void;
  setErrors: (errors: Partial<Record<keyof T, string>>) => void;
  validateForm: () => Promise<boolean>;
  validateField: (name: keyof T) => Promise<string | undefined>;
  resetForm: () => void;
  submitForm: (e?: React.FormEvent<HTMLFormElement>) => Promise<void>;
  reset: () => void;
}

/**
 * useForm hook for form state management
 */
export const useForm = <T extends Record<string, any>>(
  options: UseFormOptions<T>
): UseFormResult<T> => {
  const { initialValues, validate, onSubmit, onSuccess, onError } = options;

  const [formState, setFormState] = useState<FormState<T>>({
    values: initialValues,
    touched: {},
    errors: {},
    isSubmitting: false,
    isDirty: false,
  });

  const initialValuesRef = useRef(initialValues);

  /**
   * Check if form is dirty (changed from initial values)
   */
  const isDirty = useCallback((): boolean => {
    return JSON.stringify(formState.values) !==
      JSON.stringify(initialValuesRef.current)
      ? true
      : false;
  }, [formState.values]);

  /**
   * Check if form is valid
   */
  const isValid = useCallback((): boolean => {
    return Object.keys(formState.errors).length === 0;
  }, [formState.errors]);

  /**
   * Set field value
   */
  const setFieldValue = useCallback(
    (name: keyof T, value: any) => {
      setFormState((prev) => ({
        ...prev,
        values: {
          ...prev.values,
          [name]: value,
        },
        isDirty: isDirty(),
      }));
    },
    [isDirty]
  );

  /**
   * Set field error
   */
  const setFieldError = useCallback((name: keyof T, error: string) => {
    setFormState((prev) => ({
      ...prev,
      errors: {
        ...prev.errors,
        [name]: error,
      },
    }));
  }, []);

  /**
   * Set field touched
   */
  const setFieldTouched = useCallback(
    (name: keyof T, touched: boolean) => {
      setFormState((prev) => ({
        ...prev,
        touched: {
          ...prev.touched,
          [name]: touched,
        },
      }));
    },
    []
  );

  /**
   * Set multiple field values
   */
  const setValues = useCallback(
    (values: Partial<T>) => {
      setFormState((prev) => ({
        ...prev,
        values: {
          ...prev.values,
          ...values,
        },
        isDirty: isDirty(),
      }));
    },
    [isDirty]
  );

  /**
   * Set multiple field errors
   */
  const setErrors = useCallback(
    (errors: Partial<Record<keyof T, string>>) => {
      setFormState((prev) => ({
        ...prev,
        errors,
      }));
    },
    []
  );

  /**
   * Validate entire form
   */
  const validateForm = useCallback(async (): Promise<boolean> => {
    if (!validate) {
      return true;
    }

    try {
      const errors = await validate(formState.values);
      setErrors(errors);
      return Object.keys(errors).length === 0;
    } catch (error) {
      console.error('Form validation error:', error);
      return false;
    }
  }, [formState.values, validate, setErrors]);

  /**
   * Validate specific field
   */
  const validateField = useCallback(
    async (name: keyof T): Promise<string | undefined> => {
      if (!validate) {
        return undefined;
      }

      try {
        const errors = await validate({ ...formState.values });
        const error = errors[name];

        if (error) {
          setFieldError(name, error);
        } else {
          setFieldError(name, '');
        }

        return error;
      } catch (error) {
        console.error(`Field validation error for ${String(name)}:`, error);
        return undefined;
      }
    },
    [formState.values, validate, setFieldError]
  );

  /**
   * Handle field change
   */
  const handleFieldChange = useCallback(
    (
      e: React.ChangeEvent<
        HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement
      >
    ) => {
      const { name, value, type } = e.target;

      let fieldValue: any = value;

      // Handle checkbox
      if (type === 'checkbox') {
        fieldValue = (e.target as HTMLInputElement).checked;
      }

      // Handle number input
      if (type === 'number') {
        fieldValue = value ? parseFloat(value) : '';
      }

      setFieldValue(name as keyof T, fieldValue);
    },
    [setFieldValue]
  );

  /**
   * Handle field blur
   */
  const handleFieldBlur = useCallback(
    (
      e: React.FocusEvent<
        HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement
      >
    ) => {
      const { name } = e.target;
      setFieldTouched(name as keyof T, true);
      validateField(name as keyof T);
    },
    [setFieldTouched, validateField]
  );

  /**
   * Get field props for input binding
   */
  const getFieldProps = useCallback(
    (name: keyof T) => ({
      name: String(name),
      value: formState.values[name],
      onChange: handleFieldChange,
      onBlur: handleFieldBlur,
    }),
    [formState.values, handleFieldChange, handleFieldBlur]
  );

  /**
   * Get field meta (value, error, touched)
   */
  const getFieldMeta = useCallback(
    (name: keyof T) => ({
      value: formState.values[name],
      error: formState.errors[name],
      touched: formState.touched[name] || false,
    }),
    [formState.values, formState.errors, formState.touched]
  );

  /**
   * Reset form to initial values
   */
  const resetForm = useCallback(() => {
    initialValuesRef.current = formState.values;
    setFormState((prev) => ({
      ...prev,
      touched: {},
      errors: {},
      isDirty: false,
      isSubmitting: false,
    }));
  }, []);

  /**
   * Reset to initial values
   */
  const reset = useCallback(() => {
    setFormState({
      values: initialValuesRef.current,
      touched: {},
      errors: {},
      isSubmitting: false,
      isDirty: false,
    });
  }, []);

  /**
   * Submit form
   */
  const submitForm = useCallback(
    async (e?: React.FormEvent<HTMLFormElement>) => {
      e?.preventDefault();

      setFormState((prev) => ({
        ...prev,
        isSubmitting: true,
      }));

      try {
        // Validate form
        const isValid = await validateForm();
        if (!isValid) {
          return;
        }

        // Submit
        await onSubmit(formState.values);

        onSuccess?.();

        // Mark as not dirty after successful submit
        setFormState((prev) => ({
          ...prev,
          isDirty: false,
          isSubmitting: false,
        }));
      } catch (error) {
        const err = error instanceof Error ? error : new Error(String(error));
        onError?.(err);

        setFormState((prev) => ({
          ...prev,
          isSubmitting: false,
        }));
      }
    },
    [formState.values, validateForm, onSubmit, onSuccess, onError]
  );

  return {
    values: formState.values,
    touched: formState.touched,
    errors: formState.errors,
    isSubmitting: formState.isSubmitting,
    isDirty: isDirty(),
    isValid: isValid(),
    getFieldProps,
    getFieldMeta,
    setFieldValue,
    setFieldError,
    setFieldTouched,
    setValues,
    setErrors,
    validateForm,
    validateField,
    resetForm,
    submitForm,
    reset,
  };
};
