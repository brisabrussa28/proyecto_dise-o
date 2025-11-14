// Simple stats API client with graceful mock fallback
export type StatKey =
  | 'coleccion_por_provincia'
  | 'mayor_categoria'
  | 'provincia_por_categoria'
  | 'hora_por_categoria'
  | 'spam_eliminacion';

export type DataPoint = { label: string; value: number };

const ENDPOINTS: Record<StatKey, string> = {
  coleccion_por_provincia: '/stats/coleccion-por-provincia',
  mayor_categoria: '/stats/mayor-categoria',
  provincia_por_categoria: '/stats/provincia-por-categoria',
  hora_por_categoria: '/stats/hora-por-categoria',
  spam_eliminacion: '/stats/spam-eliminacion',
};

// Fallback mock generator while real endpoints are not available
async function mockFetch(key: StatKey): Promise<DataPoint[]> {
  await new Promise((r) => setTimeout(r, 400));
  switch (key) {
    case 'coleccion_por_provincia':
      return [
        { label: 'Buenos Aires', value: 43 },
        { label: 'Córdoba', value: 18 },
        { label: 'Santa Fe', value: 22 },
        { label: 'Mendoza', value: 11 },
        { label: 'Salta', value: 9 },
      ];
    case 'mayor_categoria':
      return [
        { label: 'Robos', value: 64 },
        { label: 'Obras', value: 28 },
        { label: 'Incidentes', value: 52 },
        { label: 'Eventos', value: 20 },
        { label: 'Dato', value: 14 },
      ];
    case 'provincia_por_categoria':
      return [
        { label: 'Buenos Aires', value: 40 },
        { label: 'Córdoba', value: 14 },
        { label: 'Santa Fe', value: 16 },
      ];
    case 'hora_por_categoria':
      return [
        { label: '00-03', value: 6 },
        { label: '03-06', value: 4 },
        { label: '06-09', value: 10 },
        { label: '09-12', value: 15 },
        { label: '12-15', value: 22 },
        { label: '15-18', value: 18 },
        { label: '18-21', value: 26 },
        { label: '21-24', value: 12 },
      ];
    case 'spam_eliminacion':
      return [
        { label: 'Spam', value: 37 },
        { label: 'Válidos', value: 63 },
      ];
    default:
      return [];
  }
}

export async function fetchStats(
    tipo: StatKey,
    filtros: { categoria?: string; coleccion?: string }
): Promise<DataPoint[]> {
    const params = new URLSearchParams({ tipo });

    if (filtros.categoria) params.append('categoria', filtros.categoria);
    if (filtros.coleccion) params.append('coleccion', filtros.coleccion);

    const res = await fetch(`/estadisticas?${params.toString()}`);

    if (!res.ok) throw new Error('Error al obtener estadísticas');

    return await res.json();
}

