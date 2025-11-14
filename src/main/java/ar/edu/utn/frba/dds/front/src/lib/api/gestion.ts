import client from "../http/client";
import { ENDPOINT_SOLICITUDES, ENDPOINT_COLECCIONES, ENDPOINT_ESTADISTICAS } from "../config/env";
import type { Solicitud, SolicitudCreate, Coleccion, ColeccionCreate, EstadisticaRespuesta } from "../types/gestion";

export async function crearSolicitud(payload: SolicitudCreate): Promise<Solicitud> {
  const { data } = await client.post<Solicitud>(ENDPOINT_SOLICITUDES, payload);
  return data;
}

export async function aprobarSolicitud(id: string | number): Promise<Solicitud> {
  const { data } = await client.put<Solicitud>(ENDPOINT_SOLICITUDES, undefined, { params: { id, aceptada: true } });
  return data;
}

export async function rechazarSolicitud(id: string | number): Promise<Solicitud> {
  const { data } = await client.put<Solicitud>(ENDPOINT_SOLICITUDES, undefined, { params: { id, aceptada: false } });
  return data;
}

export async function crearColeccion(payload: ColeccionCreate): Promise<Coleccion> {
  const { data } = await client.post<Coleccion>(ENDPOINT_COLECCIONES, payload);
  return data;
}

export async function consultarEstadistica(nombre: string, params?: Record<string, any>): Promise<EstadisticaRespuesta> {
  const base = ENDPOINT_ESTADISTICAS.endsWith("/") ? ENDPOINT_ESTADISTICAS.slice(0, -1) : ENDPOINT_ESTADISTICAS;
  const { data } = await client.get<EstadisticaRespuesta>(`${base}/${encodeURIComponent(nombre)}`, { params });
  return data;
}
