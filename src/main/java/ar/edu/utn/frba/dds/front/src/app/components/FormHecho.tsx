"use client";
import React, {useState, useEffect} from 'react';
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
    fecha_suceso: string;
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
        fecha_suceso: initialData?.fecha_suceso || '',
    });

    const [categorias, setCategorias] = useState<string[]>([]);

    useEffect(() => {
        fetch('http://localhost:9001/hechos/categorias')
            .then(res => res.json())
            .then(data => setCategorias(data))
            .catch(() => setCategorias([]));
    }, []);

    const submit = (e: React.FormEvent) => {
        e.preventDefault();

        const etiquetas = (form.etiquetas || '')
            .split(',')
            .map((s) => s.trim())
            .filter(Boolean);

        const latitud = form.lat ? parseFloat(form.lat) : 0;
        const longitud = form.lng ? parseFloat(form.lng) : 0;

        let fechaCompleta = form.fecha_suceso;
        if (fechaCompleta && !fechaCompleta.includes(':')) {
            fechaCompleta += 'T00:00:00';
        } else if (fechaCompleta && fechaCompleta.split('T')[1]?.length === 5) {
            fechaCompleta += ':00';
        }

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
            hecho_fecha_suceso: fechaCompleta || null,
        };
        onSubmit?.(payload);
    };

    return (
        <form className={styles.form} onSubmit={submit}>
            <div className={styles.row}>
                <label>Título</label>
                <input
                    value={form.titulo}
                    onChange={(e) => setForm({...form, titulo: e.target.value})}
                    placeholder="Título del hecho"
                    required
                />
            </div>
            <div className={styles.row}>
                <label>Descripción</label>
                <textarea
                    value={form.descripcion}
                    onChange={(e) => setForm({...form, descripcion: e.target.value})}
                    placeholder="Describe el hecho"
                    rows={4}
                />
            </div>

            <div className={styles.grid2}>
                <div className={styles.row}>
                    <label>Categoría</label>
                        <input
                            list="categorias-list"
                            value={form.categoria}
                            onChange={(e) => setForm({...form, categoria: e.target.value})}
                            placeholder="Selecciona o escribe una categoría"
                        />
                        <datalist id="categorias-list">
                            <option value="">Selecciona una categoría</option>
                            {categorias.map((c) => (
                                <option key={c} value={c}>{c}</option>
                            ))}
                        </datalist>
                </div>
                <div className={styles.row}>
                    <label>Dirección (opcional)</label>
                    <input
                        value={form.direccion}
                        onChange={(e) => setForm({...form, direccion: e.target.value})}
                        placeholder="Calle 123, Ciudad"
                    />
                </div>
            </div>

            <div className={styles.grid2}>
                <div className={styles.row}>
                    <label>Latitud</label>
                    <input type="number" step="any" min={-90} max={90} required value={form.lat}
                           onChange={(e) => setForm({...form, lat: e.target.value})}/>
                </div>
                <div className={styles.row}>
                    <label>Longitud</label>
                    <input type="number" step="any" min={-180} max={180} required value={form.lng}
                           onChange={(e) => setForm({...form, lng: e.target.value})}/>
                </div>
            </div>

            <div className={styles.row}>
                <label>Etiquetas (separadas por coma)</label>
                <input
                    value={form.etiquetas}
                    onChange={(e) => setForm({...form, etiquetas: e.target.value})}
                    placeholder="robo, zona-sur, auto"
                />
            </div>

            <div className={styles.fechaInput} onClick={() => document.getElementById('fechaSuceso')?.focus()}>
                <label>Fecha del Suceso</label>
                <div className={styles.fechaWrapper}>
                    <input
                        type="date"
                        id="fechaSuceso"
                        className={styles.fecha}
                        value={form.fecha_suceso.split('T')[0] || ''}
                        onChange={(e) => {
                            const hora = form.fecha_suceso.split('T')[1] || '00:00';
                            setForm({...form, fecha_suceso: `${e.target.value}T${hora}`});
                        }}
                        required
                    />
                    <span className={styles.flechita}></span>
                </div>

                <div className={styles.fechaWrapper}>
                    <input
                        type="time"
                        className={styles.fecha}
                        value={form.fecha_suceso.split('T')[1] || ''}
                        onChange={(e) => {
                            const fecha = form.fecha_suceso.split('T')[0] || '';
                            setForm({...form, fecha_suceso: `${fecha}T${e.target.value}`});
                        }}
                        required
                    />
                    <span className={styles.flechita}></span>
                </div>
            </div>

            <UploadAdjunto/>

            <div className={styles.actions}>
                <button type="submit">Crear Hecho</button>
            </div>
        </form>
    );
}