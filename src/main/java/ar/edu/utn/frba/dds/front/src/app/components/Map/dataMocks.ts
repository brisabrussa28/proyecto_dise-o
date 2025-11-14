export type HechoFeature = {
  id: string;
  titulo: string;
  descripcion: string;
  fechaISO: string; // YYYY-MM-DD
  categoria: 'Obra' | 'Incidente' | 'Evento' | 'Dato';
  fuente: 'Boletin' | 'Dataset' | 'Usuario' | 'Otro';
  coleccion: 'Obras Publicas' | 'Incidentes Viales' | 'Eventos Culturales' | 'Infraestructura';
  coords: { lat: number; lng: number };
  estado: 'Publicado' | 'Pendiente' | 'Rechazado';
  autor: string;
  adjuntos?: Array<{ id: string; url: string; tipo: 'imagen' | 'video' }>;
};

export const MOCK_HECHOS: HechoFeature[] = [
  // AMBA / Buenos Aires
  {
    id: 'H-001',
    titulo: 'Bache importante',
    descripcion: 'Reporte de bache en avenida principal',
    fechaISO: '2024-10-12',
    categoria: 'Incidente',
    fuente: 'Usuario',
    coleccion: 'Incidentes Viales',
    coords: { lat: -34.6037, lng: -58.3816 }, // CABA
    estado: 'Publicado',
    autor: 'contrib1',
    adjuntos: [
      { id: 'h001-img1', url: 'https://letraslibres.com/wp-content/uploads/2022/08/bache-gz.jpg', tipo: 'imagen' },
    ],
  },
  {
    id: 'H-002',
    titulo: 'Obra de repavimentacion',
    descripcion: 'Inicio de obra con desvios programados',
    fechaISO: '2024-09-28',
    categoria: 'Obra',
    fuente: 'Usuario',
    coleccion: 'Infraestructura',
    coords: { lat: -34.9205, lng: -57.9536 }, // La Plata
    estado: 'Publicado',
    autor: 'admin',
  },
  // Centro
  {
    id: 'H-003',
    titulo: 'Festival provincial',
    descripcion: 'Evento cultural al aire libre',
    fechaISO: '2024-11-02',
    categoria: 'Evento',
    fuente: 'Otro',
    coleccion: 'Eventos Culturales',
    coords: { lat: -31.4201, lng: -64.1888 }, // Cordoba
    estado: 'Pendiente',
    autor: 'contrib2',
    adjuntos: [
      { id: 'h003-img1', url: 'https://www.santafecultura.gob.ar/wp-content/uploads/sites/81/2025/01/Fiesta-Provincial-del-Trigo-de-San-Genaro-2.jpeg', tipo: 'imagen' },
      { id: 'h003-img2', url: 'https://silvercoder.rionegro.gov.ar/files/1671126157_7a0ac6c76739e4df09fb.jpg', tipo: 'imagen' },
    ],
  },
  {
    id: 'H-004',
    titulo: 'Datos de red pluvial',
    descripcion: 'Actualizacion de sumideros y desagues',
    fechaISO: '2024-08-15',
    categoria: 'Dato',
    fuente: 'Dataset',
    coleccion: 'Infraestructura',
    coords: { lat: -32.9442, lng: -60.6505 }, // Rosario
    estado: 'Publicado',
    autor: 'datasci',
  },
  // Cuyo
  {
    id: 'H-005',
    titulo: 'Obra de saneamiento',
    descripcion: 'Mejora de red cloacal',
    fechaISO: '2024-11-21',
    categoria: 'Obra',
    fuente: 'Usuario',
    coleccion: 'Infraestructura',
    coords: { lat: -32.8895, lng: -68.8458 }, // Mendoza
    estado: 'Pendiente',
    autor: 'contrib3',
  },
  // NOA
  {
    id: 'H-006',
    titulo: 'Incidente en ruta',
    descripcion: 'Corte parcial por derrumbe',
    fechaISO: '2024-10-05',
    categoria: 'Incidente',
    fuente: 'Usuario',
    coleccion: 'Incidentes Viales',
    coords: { lat: -24.7821, lng: -65.4232 }, // Salta
    estado: 'Publicado',
    autor: 'contrib4',
  },
  {
    id: 'H-007',
    titulo: 'Feria regional',
    descripcion: 'Encuentro de productores locales',
    fechaISO: '2024-09-10',
    categoria: 'Evento',
    fuente: 'Otro',
    coleccion: 'Eventos Culturales',
    coords: { lat: -26.8241, lng: -65.2226 }, // San Miguel de Tucuman
    estado: 'Publicado',
    autor: 'contrib5',
  },
  // NEA
  {
    id: 'H-008',
    titulo: 'Centro de monitoreo',
    descripcion: 'Nuevo punto de control hidrico',
    fechaISO: '2024-07-22',
    categoria: 'Dato',
    fuente: 'Dataset',
    coleccion: 'Infraestructura',
    coords: { lat: -27.3621, lng: -55.9009 }, // Posadas
    estado: 'Publicado',
    autor: 'datasci2',
  },
  // Patagonia
  {
    id: 'H-009',
    titulo: 'Mejora vial',
    descripcion: 'Repavimentacion de tramo critico',
    fechaISO: '2024-12-01',
    categoria: 'Obra',
    fuente: 'Otro',
    coleccion: 'Infraestructura',
    coords: { lat: -41.1335, lng: -71.3103 }, // Bariloche
    estado: 'Pendiente',
    autor: 'admin2',
  },
  {
    id: 'H-010',
    titulo: 'Temporal fuerte',
    descripcion: 'Caida de arboles y cortes',
    fechaISO: '2024-06-18',
    categoria: 'Incidente',
    fuente: 'Usuario',
    coleccion: 'Incidentes Viales',
    coords: { lat: -54.8019, lng: -68.3029 }, // Ushuaia
    estado: 'Publicado',
    autor: 'contrib6',
  },
];
