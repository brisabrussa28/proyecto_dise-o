import client from "../http/client";
import type { Hecho, HechoCreate } from "../types/hechos";
import { ENDPOINT_HECHOS } from "../config/env";

export async function crearHecho(payload: HechoCreate): Promise<Hecho> {
  const { data } = await client.post<Hecho>(ENDPOINT_HECHOS, payload);
  return data;
}

export async function obtenerHechos(params?: Record<string, any>): Promise<Hecho[]> {
  const { data } = await client.get<Hecho[]>(ENDPOINT_HECHOS, { params });
  return data;
}

export async function obtenerHecho(id: string | number): Promise<Hecho> {
  const { data } = await client.get<Hecho>(`${ENDPOINT_HECHOS}/${id}`);
  return data;
}
