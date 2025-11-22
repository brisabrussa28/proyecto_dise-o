export type Estadistica = {
    id: number;
    nombre: string;
    tipo: string;
    categoria: string | null;
    valor: number;
};

export async function fetchEstadisticas(): Promise<Estadistica[]> {
    try {
        const res = await fetch('http://localhost:9001/estadisticas', {
            method: 'GET',
            credentials: 'include',
        });
        if (!res.ok) throw new Error('Error al obtener estadísticas');
        return await res.json();
    } catch (err) {
        console.error('Fallo al cargar estadísticas:', err);
        return [];
    }
}