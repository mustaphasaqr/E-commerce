/**
 * Shared Validators
 * Input validation functions and rules
 */

/**
 * Validate email format
 */
export const validateEmail = (email: string): boolean => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
};

/**
 * Validate password strength
 * @returns object with strength level and requirements met
 */
export const validatePassword = (password: string) => {
  const requirements = {
    minLength: password.length >= 8,
    hasUppercase: /[A-Z]/.test(password),
    hasLowercase: /[a-z]/.test(password),
    hasNumbers: /\d/.test(password),
    hasSpecialChar: /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(password),
  };

  const metRequirements = Object.values(requirements).filter(Boolean).length;
  const strength =
    metRequirements >= 5
      ? 'strong'
      : metRequirements >= 3
        ? 'medium'
        : 'weak';

  return {
    isValid: metRequirements >= 4,
    strength,
    requirements,
    score: (metRequirements / 5) * 100,
  };
};

/**
 * Validate username
 * Alphanumeric, underscore, hyphen only, 3-20 chars
 */
export const validateUsername = (username: string): boolean => {
  const usernameRegex = /^[a-zA-Z0-9_-]{3,20}$/;
  return usernameRegex.test(username);
};

/**
 * Validate URL
 */
export const validateUrl = (url: string): boolean => {
  try {
    new URL(url);
    return true;
  } catch {
    return false;
  }
};

/**
 * Validate credit card number (Luhn algorithm)
 */
export const validateCreditCard = (cardNumber: string): boolean => {
  const cleaned = cardNumber.replace(/\D/g, '');
  if (cleaned.length < 13 || cleaned.length > 19) return false;

  let sum = 0;
  let isEven = false;

  for (let i = cleaned.length - 1; i >= 0; i--) {
    let digit = parseInt(cleaned.charAt(i), 10);

    if (isEven) {
      digit *= 2;
      if (digit > 9) {
        digit -= 9;
      }
    }

    sum += digit;
    isEven = !isEven;
  }

  return sum % 10 === 0;
};

/**
 * Validate phone number (basic)
 * Accepts various formats
 */
export const validatePhoneNumber = (phone: string): boolean => {
  const phoneRegex = /^[\d\s\-\+\(\)]{10,}$/;
  return phoneRegex.test(phone);
};

/**
 * Validate zip code (US)
 * Accepts 5-digit or 5+4 format
 */
export const validateZipCode = (zipCode: string): boolean => {
  const zipRegex = /^\d{5}(-\d{4})?$/;
  return zipRegex.test(zipCode);
};

/**
 * Validate required field (not empty)
 */
export const validateRequired = (value: string | number | undefined | null): boolean => {
  if (typeof value === 'number') return true;
  return Boolean(value && value.toString().trim().length > 0);
};

/**
 * Validate minimum length
 */
export const validateMinLength = (
  value: string | undefined | null,
  minLength: number
): boolean => {
  return Boolean(value && value.length >= minLength);
};

/**
 * Validate maximum length
 */
export const validateMaxLength = (
  value: string | undefined | null,
  maxLength: number
): boolean => {
  return Boolean(!value || value.length <= maxLength);
};

/**
 * Validate minimum value (number)
 */
export const validateMin = (
  value: number | undefined | null,
  min: number
): boolean => {
  return value !== undefined && value !== null && value >= min;
};

/**
 * Validate maximum value (number)
 */
export const validateMax = (
  value: number | undefined | null,
  max: number
): boolean => {
  return value !== undefined && value !== null && value <= max;
};

/**
 * Validate number range
 */
export const validateRange = (
  value: number | undefined | null,
  min: number,
  max: number
): boolean => {
  return validateMin(value, min) && validateMax(value, max);
};

/**
 * Validate that value matches pattern
 */
export const validatePattern = (
  value: string | undefined | null,
  pattern: RegExp
): boolean => {
  return Boolean(value && pattern.test(value));
};

/**
 * Validate IPv4 address
 */
export const validateIPv4 = (ip: string): boolean => {
  const ipv4Regex = /^(\d{1,3}\.){3}\d{1,3}$/;
  if (!ipv4Regex.test(ip)) return false;

  const parts = ip.split('.');
  return parts.every((part) => {
    const num = parseInt(part, 10);
    return num >= 0 && num <= 255;
  });
};

/**
 * Validate date format
 */
export const validateDate = (dateString: string): boolean => {
  const date = new Date(dateString);
  return date instanceof Date && !isNaN(date.getTime());
};

/**
 * Validate dates - end date after start date
 */
export const validateDateRange = (
  startDate: Date | string,
  endDate: Date | string
): boolean => {
  const start = new Date(startDate);
  const end = new Date(endDate);
  return start < end;
};

/**
 * Comprehensive validation object builder
 */
export const createValidator = (rules: { [key: string]: (value: unknown) => boolean }) => {
  return (data: Record<string, unknown>) => {
    const errors: { [key: string]: string } = {};

    for (const [field, validator] of Object.entries(rules)) {
      if (!validator(data[field])) {
        errors[field] = `${field} is invalid`;
      }
    }

    return {
      isValid: Object.keys(errors).length === 0,
      errors,
    };
  };
};
