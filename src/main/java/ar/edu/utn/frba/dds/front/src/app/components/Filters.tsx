"use client";
import React, { useEffect, useState } from 'react';
import { useSearchParams, useRouter } from 'next/navigation';
import styles from '../css/Filters.module.css';

export type FiltersState = {
  categoria: string | 'Todas';
  fuente: string | 'Todas';
  coleccion: string | 'Todas';
  desde: string; // YYYY-MM-DD
  hasta: string; // YYYY-MM-DD
};

export default function Filters({
                                    resultados,
                                    variant = 'floating',
                                }: {
    resultados: number;
    variant?: 'floating' | 'inline';
}) {
    const params = useSearchParams();
    const router = useRouter();

    const getInitialFilters = (): FiltersState => ({
        categoria: params.get('categoria') || 'Todas',
        fuente: params.get('fuente') || 'Todas',
        coleccion: params.get('coleccion') || 'Todas',
        desde: params.get('desde') || '',
        hasta: params.get('hasta') || '',
    });

    const [filters, setFilters] = useState<FiltersState>(getInitialFilters());

    // Actualiza la URL y el estado local
    const updateFilters = (next: FiltersState) => {
        setFilters(next);

        const query = new URLSearchParams();

        if (next.categoria !== 'Todas') query.set('categoria', next.categoria);
        if (next.fuente !== 'Todas') query.set('fuente', next.fuente);
        if (next.coleccion !== 'Todas') query.set('coleccion', next.coleccion);
        if (next.desde) query.set('desde', next.desde);
        if (next.hasta) query.set('hasta', next.hasta);

        router.replace(`?${query.toString()}`);
    };

    return (
        <div className={variant === 'inline' ? styles.inline : styles.panel}>
            <div className={styles.rowFiltros}>
                <label>Categoría</label>
                <select
                    value={filters.categoria}
                    onChange={(e) => updateFilters({ ...filters, categoria: e.target.value })}
                >
                    <option value="Todas">Todas</option>
                    <option value="Obra">Obra</option>
                    <option value="Incidente">Incidente</option>
                    <option value="Evento">Evento</option>
                    <option value="Dato">Dato</option>
                </select>
            </div>

            <div className={styles.rowFiltros}>
                <label>Fuente</label>
                <select
                    value={filters.fuente}
                    onChange={(e) => updateFilters({ ...filters, fuente: e.target.value })}
                >
                    <option value="Todas">Todas</option>
                    <option value="Boletín">Boletín</option>
                    <option value="Dataset">Dataset</option>
                    <option value="Usuario">Usuario</option>
                    <option value="Otro">Otro</option>
                </select>
            </div>

            <div className={styles.rowFiltros}>
                <label>Colección</label>
                <select
                    value={filters.coleccion}
                    onChange={(e) => updateFilters({ ...filters, coleccion: e.target.value })}
                >
                    <option value="Todas">Todas</option>
                    <option value="Obras Públicas">Obras Públicas</option>
                    <option value="Incidentes Viales">Incidentes Viales</option>
                    <option value="Eventos Culturales">Eventos Culturales</option>
                    <option value="Infraestructura">Infraestructura</option>
                </select>
            </div>

            <div className={styles.rowFiltrosFechas}>
                <label>Fecha</label>
                <div className={styles.fechaInput}>
                    <span>Desde</span>
                    <input
                        type="date"
                        className={styles.fecha}
                        value={filters.desde}
                        onChange={(e) => updateFilters({ ...filters, desde: e.target.value })}
                    />
                </div>
                <div className={styles.fechaInput}>
                    <span>Hasta</span>
                    <input
                        type="date"
                        className={styles.fecha}
                        value={filters.hasta}
                        onChange={(e) => updateFilters({ ...filters, hasta: e.target.value })}
                    />
                </div>
            </div>

            <div className={styles.footer}>
                <button
                    className={styles.reset}
                    onClick={() =>
                        updateFilters({ categoria: 'Todas', fuente: 'Todas', coleccion: 'Todas', desde: '', hasta: '' })
                    }
                >
                    Limpiar
                </button>
            </div>
        </div>
    );
}
