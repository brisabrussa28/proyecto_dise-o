import client from "../http/client";
import type { Categoria, Fuente } from "../types/catalogos";
import { ENDPOINT_CATEGORIAS, ENDPOINT_FUENTES } from "../config/env";

export async function obtenerCategorias(): Promise<Categoria[]> {
  const { data } = await client.get<Categoria[]>(ENDPOINT_CATEGORIAS);
  return data;
}

export async function obtenerFuentes(): Promise<Fuente[]> {
  const { data } = await client.get<Fuente[]>(ENDPOINT_FUENTES);
  return data;
}
