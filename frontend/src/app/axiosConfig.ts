import axios from "axios";

// Use Railway backend for tests and production
const baseURL = "https://e-commerce-production-27b3.up.railway.app/";

const axiosConfig = axios.create({
  baseURL,
});

export default axiosConfig;
