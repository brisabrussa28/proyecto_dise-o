"use client";
import React from 'react';
import {useRouter} from 'next/navigation';
import styles from '../css/Modal.module.css';

type Hecho = {
    id: string;
    titulo: string;
    descripcion: string;
    fecha?: string;
    categoria?: string;
    direccion?: string | null;
    etiquetas?: string[];
    ubicacion?: { lat: number; lng: number };
    adjuntos?: Array<{ id: string; url: string; }>;
};

export default function HechoDetalleModal({
                                              open,
                                              onClose,
                                              hecho,
                                              onViewOnMap,
                                          }: {
    open: boolean;
    onClose: () => void;
    hecho: Hecho | null | undefined;
    onViewOnMap?: () => void;
}) {
    if (!open || !hecho) return null;

    const firstAdj = hecho.adjuntos && hecho.adjuntos.length ? hecho.adjuntos[0] : null;
    const router = useRouter();

    return (
        <div className={styles.overlay} onClick={onClose}>
            <div className={styles.modal} onClick={(e) => e.stopPropagation()}>
                <div className={styles.header}>
                    <h3>{hecho.titulo}</h3>
                    <button className={styles.close} onClick={onClose} aria-label="Cerrar">×</button>
                </div>

                <div className={styles.content}>
                    {firstAdj && (
                        <div className={styles.cover}>
                            // eslint-disable-next-line @next/next/no-img-element
                            <img src={firstAdj.url} alt="Imagen del hecho"/>
                        </div>
                    )}

                    <div className={styles.descripcionBox}>{hecho.descripcion}</div>

                    <p className={styles.meta}><strong>Fecha:</strong> {hecho.fecha || '-'}</p>
                    <p className={styles.meta}><strong>Dirección:</strong> {hecho.direccion ?? '-'}</p>
                    {hecho.ubicacion && (
                        <p className={styles.meta}>
                            <strong>Ubicación:</strong> {hecho.ubicacion.lat.toFixed(2)}, {hecho.ubicacion.lng.toFixed(2)}
                        </p>
                    )}
                    <p className={styles.meta}><strong>Categoría:</strong> {hecho.categoria || '-'}</p>
                    <p className={styles.meta}><strong>ID:</strong> {hecho.id}</p>

                    {!!hecho.etiquetas?.length && (
                        <div className={styles.tags}>
                            {hecho.etiquetas.map((t) => (
                                <span key={t} className={styles.tag}>#{t}</span>
                            ))}
                        </div>
                    )}
                </div>

                <div className={styles.actions}>
                    <button
                        className={styles.secondaryBtn}
                        onClick={() => onViewOnMap?.()}
                    >
                        Ver en Mapa
                    </button>
                    <button
                        className={styles.primaryBtn}
                        onClick={() => router.push(`/hecho/${encodeURIComponent(hecho.id)}/reportar`)}
                    >
                        Reportar
                    </button>
                </div>
            </div>
        </div>
    );
}
