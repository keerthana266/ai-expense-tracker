import axios from "axios";
const api = axios.create({
  baseURL: "http://localhost:8080/api",
  timeout: 30000,
  headers: {
    "Content-Type": "application/json",
  },
});
// ===============================
// REQUEST INTERCEPTOR
// ===============================
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("token");

    if (token) {
      config.headers = config.headers || {};
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// ===============================
// RESPONSE INTERCEPTOR
// ===============================

api.interceptors.response.use(
  (response) => {
    return response;
  },

  (error) => {
    if (error.response) {
      // Only redirect if genuinely unauthorized (401) and NOT during auth login/signup requests
      const isAuthEndpoint = error.config?.url?.includes("/auth/");
      if (error.response.status === 401 && !isAuthEndpoint) {
        console.warn("🔒 Token expired. Clearing session.");
        localStorage.removeItem("token");
        localStorage.removeItem("email");
        window.location.href = "/login";
      }
    }
    return Promise.reject(error);
  }
);
export default api;
