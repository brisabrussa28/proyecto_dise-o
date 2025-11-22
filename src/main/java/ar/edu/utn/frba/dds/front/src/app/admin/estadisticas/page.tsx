"use client";
import React, { useEffect, useMemo, useState } from 'react';
import { useSession } from '../../components/SessionContext';
import styles from '../../css/Stats.module.css';
import { fetchEstadisticas, type Estadistica } from '../../lib/statsApi';

export default function EstadisticasPage() {
    const { role } = useSession();
    const [estadisticas, setEstadisticas] = useState<Estadistica[]>([]);
    const [tipoSeleccionado, setTipoSeleccionado] = useState('');
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        setLoading(true);
        fetchEstadisticas().then((data) => {
            setEstadisticas(data);
            setLoading(false);
        });
    }, []);

    const tipos = useMemo(() => {
        const todos = estadisticas.map((e) => e.tipo);
        return Array.from(new Set(todos));
    }, [estadisticas]);

    const filtradas = useMemo(() => {
        return estadisticas.filter((e) => e.tipo === tipoSeleccionado);
    }, [estadisticas, tipoSeleccionado]);

    if (role !== 'Administrador') {
        return (
            <div className={styles.page}>
                <div className={styles.card}>
                    <div className={styles.title}>Estadísticas</div>
                    <p>Solo Administradores pueden acceder a esta vista.</p>
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
                    <select
                        className={styles.select}
                        value={tipoSeleccionado}
                        onChange={(e) => setTipoSeleccionado(e.target.value)}
                    >
                        <option value="" disabled hidden>Selecciona un tipo</option>
                        {tipos.map((t) => (
                            <option key={t} value={t}>{t}</option>
                        ))}
                    </select>
                </div>

                <div className={styles.chartWrap}>
                    <div className={styles.chartTitle}>{tipoSeleccionado || 'Selecciona un tipo para ver resultados'}</div>
                    {loading && <div className={styles.muted}>Cargando…</div>}
                    {!loading && filtradas.length === 0 && (
                        <div className={styles.empty}>No hay datos para este tipo.</div>
                    )}
                    {!!filtradas.length && !loading && (
                        <div className={styles.bars}>
                            {filtradas.map((e) => (
                                <div key={e.id} className={styles.barRow}>
                                    <div>{e.nombre}</div>
                                    <div>{e.categoria || 'Sin categoría'}</div>
                                    <div style={{ textAlign: 'right' }}>{e.valor}</div>
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}