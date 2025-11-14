import axios, { AxiosHeaders } from "axios";
import { API_BASE_URL, API_TOKEN, API_TOKEN_HEADER, TOKEN_STORAGE_KEY, isBrowser } from "../config/env";

const client = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true,
  headers: {
    "Content-Type": "application/json",
    ...(API_TOKEN ? { [API_TOKEN_HEADER]: API_TOKEN } : {}),
  },
});

client.interceptors.request.use((config) => {
  try {
    if (isBrowser) {
      const token = localStorage.getItem(TOKEN_STORAGE_KEY);
      if (token) {
        if (!config.headers) {
          config.headers = new AxiosHeaders();
        } else if (!(config.headers as any).set) {
          // Normalize plain object headers to AxiosHeaders for typed mutation
          config.headers = new AxiosHeaders(config.headers as any);
        }
        (config.headers as AxiosHeaders).set("Authorization", `Bearer ${token}`);
      }
    }
  } catch {}
  return config;
});

// Nota: aquí podríamos implementar lógica de refresh token si el backend lo soporta
client.interceptors.response.use(
  (res) => res,
  async (error) => {
    // Por ahora, sólo propagamos el error
    return Promise.reject(error);
  }
);

export default client;
