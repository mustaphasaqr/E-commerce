/**
 * Shared Formatters
 * Utility functions for formatting data (prices, dates, etc.)
 */

/**
 * Format number as currency
 * @param amount - The amount to format
 * @param currency - Currency code (default: USD)
 * @param locale - Locale string (default: en-US)
 */
export const formatPrice = (
  amount: number | undefined | null,
  currency: string = 'USD',
  locale: string = 'en-US'
): string => {
  if (amount === undefined || amount === null) return 'N/A';

  return new Intl.NumberFormat(locale, {
    style: 'currency',
    currency,
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(amount);
};

/**
 * Format number as percentage
 * @param value - The value to format (0-1)
 * @param decimals - Number of decimal places (default: 2)
 */
export const formatPercentage = (
  value: number | undefined | null,
  decimals: number = 2
): string => {
  if (value === undefined || value === null) return 'N/A';
  return `${(value * 100).toFixed(decimals)}%`;
};

/**
 * Format date to readable string
 * @param date - Date object or date string
 */
export const formatDate = (
  date: Date | string | undefined | null
): string => {
  if (!date) return 'N/A';

  const dateObj = typeof date === 'string' ? new Date(date) : date;
  if (isNaN(dateObj.getTime())) return 'Invalid date';

  const formatter = new Intl.DateTimeFormat('en-US', {
    year: 'numeric',
    month: 'short',
    day: '2-digit',
  });

  return formatter.format(dateObj);
};

/**
 * Format date and time
 * @param date - Date object or date string
 */
export const formatDateTime = (
  date: Date | string | undefined | null
): string => {
  if (!date) return 'N/A';

  const dateObj = typeof date === 'string' ? new Date(date) : date;
  if (isNaN(dateObj.getTime())) return 'Invalid date';

  const formatter = new Intl.DateTimeFormat('en-US', {
    year: 'numeric',
    month: 'short',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  });

  return formatter.format(dateObj);
};

/**
 * Format number with commas
 * @param value - The number to format
 * @param decimals - Number of decimal places
 */
export const formatNumber = (
  value: number | undefined | null,
  decimals: number = 0
): string => {
  if (value === undefined || value === null) return 'N/A';
  return value.toLocaleString('en-US', {
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals,
  });
};

/**
 * Format file size (bytes to human readable)
 * @param bytes - Size in bytes
 */
export const formatFileSize = (bytes: number | undefined | null): string => {
  if (bytes === undefined || bytes === null || bytes === 0) return '0 B';

  const k = 1024;
  const sizes = ['B', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));

  return (bytes / Math.pow(k, i)).toFixed(2) + ' ' + sizes[i];
};

/**
 * Truncate string to maxLength with ellipsis
 * @param text - Text to truncate
 * @param maxLength - Maximum length before truncation
 */
export const truncateText = (
  text: string | undefined | null,
  maxLength: number = 100
): string => {
  if (!text) return '';
  return text.length > maxLength ? `${text.substring(0, maxLength)}...` : text;
};

/**
 * Format time duration
 * @param seconds - Duration in seconds
 */
export const formatDuration = (seconds: number | undefined | null): string => {
  if (seconds === undefined || seconds === null || seconds === 0)
    return '0s';

  const hours = Math.floor(seconds / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);
  const secs = Math.floor(seconds % 60);

  const parts: string[] = [];
  if (hours > 0) parts.push(`${hours}h`);
  if (minutes > 0) parts.push(`${minutes}m`);
  if (secs > 0 || parts.length === 0) parts.push(`${secs}s`);

  return parts.join(' ');
};

/**
 * Capitalize first letter
 * @param text - Text to capitalize
 */
export const capitalize = (text: string | undefined | null): string => {
  if (!text) return '';
  return text.charAt(0).toUpperCase() + text.slice(1).toLowerCase();
};

/**
 * Format slug to readable text
 * @param slug - Slug string (e.g., "my-product-name")
 */
export const formatSlugToText = (slug: string | undefined | null): string => {
  if (!slug) return '';
  return slug
    .split('-')
    .map((word) => capitalize(word))
    .join(' ');
};

/**
 * Format phone number
 * @param phone - Phone number string
 */
export const formatPhoneNumber = (
  phone: string | undefined | null
): string => {
  if (!phone) return '';

  const cleaned = phone.replace(/\D/g, '');
  if (cleaned.length !== 10) return phone;

  return `(${cleaned.slice(0, 3)}) ${cleaned.slice(3, 6)}-${cleaned.slice(
    6
  )}`;
};
