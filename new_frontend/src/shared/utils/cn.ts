import { clsx, type ClassValue } from 'clsx'
import { twMerge } from 'tailwind-merge'

/**
 * Merge Tailwind CSS classes with clsx and tailwind-merge
 * Handles class name conflicts and merges Tailwind utilities correctly
 */
export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}
