export interface HechoUbicacion {
  latitud: number;
  longitud: number;
}

export interface HechoCreate {
  hecho_etiquetas: string[];
  hecho_estado: string; // e.g. "Original"
  hecho_fotos: string[]; // URLs o IDs según backend
  hecho_titulo: string;
  hecho_descripcion: string;
  hecho_categoria: string;
  hecho_direccion: string | null;
  hecho_ubicacion: HechoUbicacion;
  hecho_origen: string; // e.g. "Provisto_Contribuyente"
}

export type HechoEstado = "ORIGINAL" | "ELIMINADO" | "MODIFICADO";
export type HechoOrigen = "PROVISTO_CONTRIBUYENTE" | "DERIVADO" | "PROVISTO_FUENTE";

export interface Hecho {
  id: string | number;
  hecho_etiquetas: string[];
  hecho_estado: HechoEstado;
  hecho_fotos: string[];
  hecho_titulo: string;
  hecho_descripcion: string | null;
  hecho_categoria: string;
  hecho_direccion: string | null;
  hecho_ubicacion: HechoUbicacion;
  hecho_origen: HechoOrigen | string;
  hecho_provincia?: string | null;
  hecho_fecha_suceso?: string;
  hecho_fecha_carga?: string;
  creadoEn?: string;
  actualizadoEn?: string;
}
