"use client";
import React, { useMemo, useState } from 'react';
import { useSession } from '../../components/SessionContext';
import styles from '../../css/Stats.module.css';
import { fetchStats, type StatKey, type DataPoint } from '../../lib/statsApi';

const CATEGORIAS = ['Robos', 'Obras', 'Incidentes', 'Eventos', 'Dato'];
const COLECCIONES = ['Obras Públicas', 'Incidentes Viales', 'Eventos Culturales', 'Infraestructura'];

export default function EstadisticasPage() {
  const { role } = useSession();
  const [stat, setStat] = useState<StatKey>('coleccion_por_provincia');
  const [categoria, setCategoria] = useState('');
  const [coleccion, setColeccion] = useState('');
  const [data, setData] = useState<DataPoint[] | null>(null);
  const [loading, setLoading] = useState(false);

  const needsColeccion = stat === 'coleccion_por_provincia';
  const needsCategoria = stat === 'provincia_por_categoria' || stat === 'hora_por_categoria';

  const title = useMemo(() => {
    switch (stat) {
      case 'coleccion_por_provincia': return 'Hechos por provincia (según colección)';
      case 'mayor_categoria': return 'Categorías por cantidad de hechos';
      case 'provincia_por_categoria': return 'Provincias con más hechos de la categoría';
      case 'hora_por_categoria': return 'Distribución por franja horaria';
      case 'spam_eliminacion': return 'Solicitudes de eliminación consideradas spam';
    }
  }, [stat]);

    async function buscar() {
    setLoading(true);
    const result = await fetchStats(stat, { categoria: needsCategoria ? categoria : undefined, coleccion: needsColeccion ? coleccion : undefined });
    setData(result);
    setLoading(false);
  }

  if (role !== 'Administrador') {
    return (
      <div className={styles.page}>
        <div className={styles.card}>
          <div className={styles.title}>Estadísticas</div>
          <p>Solo Administradores pueden acceder a esta vista. (UI mock)</p>
        </div>
      </div>
    );
  }

  return (
    <div className={styles.page}>
      <div className={styles.card}>
        <div className={styles.title}>Estadísticas</div>

        <div className={styles.controls}>
          <span className={styles.label}>Tipo</span>
          <select className={styles.select} value={stat} onChange={(e) => setStat(e.target.value as StatKey)}>
            <option value="coleccion_por_provincia">Cantidad de Hechos por Provincia, de una Colección</option> 
            {/* De una colección, ¿en qué provincia se agrupan la mayor cantidad de hechos reportados? */}
            <option value="mayor_categoria">Cantidad de Hechos por Categorias</option>
            {/* ¿Cuál es la categoría con mayor cantidad de hechos reportados? */}
            <option value="provincia_por_categoria">Mayor cantidad de Hechos de cierta Categoria</option>
            {/* ¿En qué provincia se presenta la mayor cantidad de hechos de una cierta categoría? */}
            <option value="hora_por_categoria">Cantidad de hechos por hora en una cierta categoria</option>
            {/* ¿A qué hora del día ocurren la mayor cantidad de hechos de una cierta categoría? */}
            <option value="spam_eliminacion">Cantidad de solicitudes de eliminación que son spam</option>
            {/* ¿Cuántas solicitudes de eliminación son spam? */}
          </select>

          {needsColeccion && (
            <>
              <span className={styles.label}>Colección</span>
              <select className={styles.select} value={coleccion} onChange={(e) => setColeccion(e.target.value)}>
                <option value="">Selecciona una colección</option>
                {COLECCIONES.map((c) => <option key={c} value={c}>{c}</option>)}
              </select>
            </>
          )}

          {needsCategoria && (
            <>
              <span className={styles.label}>Categoría</span>
              <select className={styles.select} value={categoria} onChange={(e) => setCategoria(e.target.value)}>
                <option value="">Selecciona una categoría</option>
                {CATEGORIAS.map((c) => <option key={c} value={c}>{c}</option>)}
              </select>
            </>
          )}

          <button className={styles.btn} onClick={buscar}>Buscar</button>
        </div>

        <div className={styles.chartWrap}>
          <div className={styles.chartTitle}>{title}</div>
          {!data && !loading && (
            <div className={styles.empty}>Usa los controles de arriba y presiona Buscar.</div>
          )}
          {loading && (
            <div className={styles.muted}>Cargando…</div>
          )}
          {!!data?.length && !loading && (
            <div className={styles.bars}>
              {(() => {
                const max = Math.max(...data.map(d => d.value));
                return data.map((d) => (
                  <div key={d.label} className={styles.barRow}>
                    <div>{d.label}</div>
                    <div className={styles.barTrack}>
                      <div className={styles.barFill} style={{ width: `${Math.max(5, (d.value / max) * 100)}%` }} />
                    </div>
                    <div style={{ textAlign: 'right' }}>{d.value}</div>
                  </div>
                ));
              })()}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
