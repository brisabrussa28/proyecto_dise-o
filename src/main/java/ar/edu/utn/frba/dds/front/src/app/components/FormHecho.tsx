"use client";
import React, { useState } from 'react';
import styles from '../css/FormHecho.module.css';
import UploadAdjunto from './UploadAdjunto';

export type HechoInput = {
    titulo: string;
    descripcion: string;
    categoria?: string;
    direccion?: string;
    lat?: string;
    lng?: string;
    etiquetas?: string; // separadas por coma
};

export default function FormHecho({
                                      initialData,
                                      onSubmit,
                                  }: {
    initialData?: Partial<HechoInput>;
    onSubmit?: (payload: any) => void;
}) {
    const [form, setForm] = useState<HechoInput>({
        titulo: initialData?.titulo || '',
        descripcion: initialData?.descripcion || '',
        categoria: initialData?.categoria || '',
        direccion: initialData?.direccion || '',
        lat: initialData?.lat || '',
        lng: initialData?.lng || '',
        etiquetas: initialData?.etiquetas || '',
    });

    const [categorias] = useState<string[]>(['Robos', 'Obras', 'Incidentes', 'Eventos', 'Dato']);

    const submit = (e: React.FormEvent) => {
        e.preventDefault();

        const etiquetas = (form.etiquetas || '')
            .split(',')
            .map((s) => s.trim())
            .filter(Boolean);

        const latitud = form.lat ? parseFloat(form.lat) : 0;
        const longitud = form.lng ? parseFloat(form.lng) : 0;

        const payload = {
            hecho_etiquetas: etiquetas,
            hecho_estado: 'ORIGINAL',
            hecho_fotos: [],
            hecho_titulo: form.titulo,
            hecho_descripcion: form.descripcion,
            hecho_categoria: form.categoria || '',
            hecho_direccion: form.direccion || null,
            hecho_ubicacion: {
                latitud,
                longitud,
            },
            hecho_origen: 'PROVISTO_CONTRIBUYENTE',
        };

        onSubmit?.(payload);
    };

    return (
        <form className={styles.form} onSubmit={submit}>
            <div className={styles.row}>
                <label>Título</label>
                <input
                    value={form.titulo}
                    onChange={(e) => setForm({ ...form, titulo: e.target.value })}
                    placeholder="Título del hecho"
                    required
                />
            </div>
            <div className={styles.row}>
                <label>Descripción</label>
                <textarea
                    value={form.descripcion}
                    onChange={(e) => setForm({ ...form, descripcion: e.target.value })}
                    placeholder="Describe el hecho"
                    rows={4}
                />
            </div>

            <div className={styles.grid2}>
                <div className={styles.row}>
                    <label>Categoría</label>
                    <select value={form.categoria} onChange={(e) => setForm({ ...form, categoria: e.target.value })}>
                        <option value="">Selecciona una categoría</option>
                        {categorias.map((c) => (
                            <option key={c} value={c}>{c}</option>
                        ))}
                    </select>
                </div>
                <div className={styles.row}>
                    <label>Dirección (opcional)</label>
                    <input
                        value={form.direccion}
                        onChange={(e) => setForm({ ...form, direccion: e.target.value })}
                        placeholder="Calle 123, Ciudad"
                    />
                </div>
            </div>

            <div className={styles.grid2}>
                <div className={styles.row}>
                    <label>Latitud</label>
                    <input type="number" step="any" min={-90} max={90} required value={form.lat} onChange={(e) => setForm({ ...form, lat: e.target.value })} />
                </div>
                <div className={styles.row}>
                    <label>Longitud</label>
                    <input type="number" step="any" min={-180} max={180} required value={form.lng} onChange={(e) => setForm({ ...form, lng: e.target.value })} />
                </div>
            </div>

            <div className={styles.row}>
                <label>Etiquetas (separadas por coma)</label>
                <input
                    value={form.etiquetas}
                    onChange={(e) => setForm({ ...form, etiquetas: e.target.value })}
                    placeholder="robo, zona-sur, auto"
                />
            </div>

            <UploadAdjunto />

            <div className={styles.actions}>
                <button type="submit">Crear Hecho</button>
            </div>
        </form>
    );
}