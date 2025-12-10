export type Fuente = { id: string; nombre: string };
export type Coleccion = {
  id: string;
  nombre: string;
  descripcion: string;
  categoria: string;
  algoritmo: string;
  fuente: string;
};

export const MOCK_FUENTES: Fuente[] = [
  { id: 'F-001', nombre: 'Boletin Oficial' },
  { id: 'F-002', nombre: 'Dataset Publico' },
];

export const MOCK_COLECCIONES: Coleccion[] = [
  { id: 'C-001', nombre: 'Obras Publicas', descripcion: 'Obras y reparaciones urbanas', categoria: 'Obras', algoritmo: 'may_simple', fuente: 'Boletin Oficial' },
  { id: 'C-002', nombre: 'Incidentes Viales', descripcion: 'Reportes de incidentes de transito', categoria: 'Incidentes', algoritmo: 'prioridad_alta', fuente: 'Dataset Publico' },
];

export const findFuente = (id: string) => MOCK_FUENTES.find((f) => f.id === id);
export const findColeccion = (id: string) => MOCK_COLECCIONES.find((c) => c.id === id);
