export const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:3000/api";
export const API_TOKEN = process.env.NEXT_PUBLIC_API_TOKEN;
export const API_TOKEN_HEADER = process.env.NEXT_PUBLIC_API_TOKEN_HEADER || "x-api-key";

// Endpoints configurables (full URL o path relativo a baseURL)
export const ENDPOINT_AUTH_LOGIN = process.env.NEXT_PUBLIC_ENDPOINT_AUTH_LOGIN || "/auth/login";
export const ENDPOINT_AUTH_REGISTER = process.env.NEXT_PUBLIC_ENDPOINT_AUTH_REGISTER || "/auth/register";
export const ENDPOINT_CATEGORIAS = process.env.NEXT_PUBLIC_ENDPOINT_CATEGORIAS || "/categorias";
export const ENDPOINT_FUENTES = process.env.NEXT_PUBLIC_ENDPOINT_FUENTES || "/fuentes";
export const ENDPOINT_HECHOS = process.env.NEXT_PUBLIC_ENDPOINT_HECHOS || "/hechos";
export const ENDPOINT_SOLICITUDES = process.env.NEXT_PUBLIC_ENDPOINT_SOLICITUDES || "/solicitudes";
export const ENDPOINT_COLECCIONES = process.env.NEXT_PUBLIC_ENDPOINT_COLECCIONES || "/colecciones";
export const ENDPOINT_ESTADISTICAS = process.env.NEXT_PUBLIC_ENDPOINT_ESTADISTICAS || "/estadisticas";

export const TOKEN_STORAGE_KEY = "metamapa-token";
export const REFRESH_TOKEN_STORAGE_KEY = "metamapa-refresh-token";
export const isBrowser = typeof window !== "undefined";
