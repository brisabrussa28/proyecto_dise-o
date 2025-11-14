import client from "../http/client";
import { isBrowser, TOKEN_STORAGE_KEY, REFRESH_TOKEN_STORAGE_KEY, ENDPOINT_AUTH_LOGIN, ENDPOINT_AUTH_REGISTER } from "../config/env";
import type { LoginRequest, LoginResponse, RegisterRequest, RegisterResponse } from "../types/auth";

export async function login(payload: LoginRequest): Promise<LoginResponse> {
  const { data } = await client.post<LoginResponse>(ENDPOINT_AUTH_LOGIN, payload);
  try {
    if (isBrowser) {
      if (data.accessToken) localStorage.setItem(TOKEN_STORAGE_KEY, data.accessToken);
      if (data.refreshToken) localStorage.setItem(REFRESH_TOKEN_STORAGE_KEY, data.refreshToken);
    }
  } catch {}
  return data;
}

export async function register(payload: RegisterRequest): Promise<RegisterResponse> {
  const { data } = await client.post<RegisterResponse>(ENDPOINT_AUTH_REGISTER, payload);
  return data;
}

export function logout() {
  try {
    if (isBrowser) {
      localStorage.removeItem(TOKEN_STORAGE_KEY);
      localStorage.removeItem(REFRESH_TOKEN_STORAGE_KEY);
    }
  } catch {}
}
