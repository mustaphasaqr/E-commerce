import axios, { AxiosInstance, AxiosError } from 'axios'

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'
const API_TIMEOUT = import.meta.env.VITE_API_TIMEOUT || 30000

const axiosInstance: AxiosInstance = axios.create({
  baseURL: `${API_URL}/api/v1`,
  timeout: parseInt(API_TIMEOUT as string),
  headers: {
    'Content-Type': 'application/json',
  },
})

// Request interceptor - logs all API calls
axiosInstance.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('authToken')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    
    // Console monitoring - request
    console.log(`📤 API Request: ${config.method?.toUpperCase()} ${config.url}`, {
      data: config.data,
      params: config.params,
    })
    
    return config
  },
  (error: AxiosError) => {
    console.error('❌ Request Error:', error.message)
    return Promise.reject(error)
  }
)

// Response interceptor - logs all responses
axiosInstance.interceptors.response.use(
  (response) => {
    // Console monitoring - success response
    console.log(`✅ API Success: ${response.status} ${response.config.url}`, response.data)
    return response
  },
  (error: AxiosError) => {
    // Console monitoring - error response
    console.error(
      `❌ API Error: ${error.response?.status || 'Network Error'} ${error.config?.url}`,
      {
        message: error.message,
        status: error.response?.status,
        data: error.response?.data,
      }
    )
    
    if (error.response?.status === 401) {
      console.warn('⚠️ Unauthorized - clearing auth and redirecting to login')
      localStorage.removeItem('authToken')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export default axiosInstance
