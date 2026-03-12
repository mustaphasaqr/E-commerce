/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: "#3b82f6",
        secondary: "#8b5cf6",
        destructive: "#ef4444",
        muted: "#6b7280",
        success: "#10b981",
        warning: "#f59e0b",
      }
    },
  },
  plugins: [],
}
