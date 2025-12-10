export type Role = "Visitante" | "Contribuyente" | "Administrador";

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken?: string;
  role?: Role;
  user?: {
    id: string | number;
    email: string;
    nombre?: string;
  };
}

export interface RegisterRequest {
  email: string;
  password: string;
  nombre?: string;
}

export interface RegisterResponse {
  id: string | number;
  email: string;
}

