import type { Hecho } from "./hechos";

export type EstadoSolicitud = "PENDIENTE" | "APROBADA" | "RECHAZADA";

export interface Solicitud {
  id: string | number;
  estado: EstadoSolicitud;
  razonEliminacion?: string | null;
  hechoSolicitado: Hecho;
  creadoEn?: string;
  actualizadoEn?: string;
}

export interface SolicitudCreate {
  hecho_solicitado: number; // id del hecho
  motivo_solicitud: string;
}

export interface Coleccion {
  id: string | number;
  nombre?: string; // por compatibilidad
}

export interface ColeccionCreate {
  coleccion_titulo: string;
  coleccion_fuente: string; // id fuente como string
  coleccion_algoritmo: string;
  coleccion_descripcion: string;
  coleccion_categoria: string;
}

export type EstadisticaRespuesta = any; // Definir cuando se conozca el shape real
