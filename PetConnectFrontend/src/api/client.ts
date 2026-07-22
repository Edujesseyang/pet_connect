import axios from "axios";

// Point this at wherever Jesse's Spring Boot app runs locally (default 8080).
// Override by creating a .env.local with VITE_API_BASE_URL=http://localhost:8080
const baseURL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

export const apiClient = axios.create({
  baseURL,
  headers: {
    "Content-Type": "application/json",
  },
});

// Once login returns a JWT (see API_CONTRACT.md), attach it to every request.
apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem("pc_token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});
