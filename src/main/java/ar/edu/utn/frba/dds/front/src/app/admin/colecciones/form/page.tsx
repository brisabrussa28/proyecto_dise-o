"use client";
import React, { useState } from 'react';
import { useRouter } from 'next/navigation';
import { useSession } from '../../../components/SessionContext';
import styles from '../../../css/AdminConfig.module.css';

const CATEGORIAS = ['Robos', 'Obras', 'Incidentes', 'Eventos', 'Dato'];
const ALGORITMOS = ['may_simple', 'prioridad_alta', 'relevancia'];

export default function CrearColeccionPage() {
    const { role, token } = useSession();
    const router = useRouter();

    const [form, setForm] = useState({
        titulo: '',
        descripcion: '',
        categoria: '',
        algoritmo: '',
        fuente: '',
    });

    const setField = (key: keyof typeof form, value: string) =>
        setForm((prev) => ({ ...prev, [key]: value }));

    const guardarColeccion = async () => {
        if (!token) {
            alert('No estás autenticado.');
            return;
        }

        const body = {
            coleccion_titulo: form.titulo,
            coleccion_fuente: parseInt(form.fuente),
            coleccion_algoritmo: form.algoritmo,
            coleccion_descripcion: form.descripcion,
            coleccion_categoria: form.categoria,
        };

        try {
            const res = await fetch('/colecciones', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: `Bearer ${token}`,
                },
                body: JSON.stringify(body),
            });

            if (!res.ok) throw new Error('Error al guardar la colección');
            alert('Colección creada exitosamente');
            router.back();
        } catch (err) {
            console.error(err);
            alert('No se pudo conectar con el servidor');
        }
    };

    if (role !== 'Administrador') {
        return <p>No autorizado</p>;
    }

    return (
        <div className={styles.page}>
            <div className={styles.formWrap}>
                <div className={styles.formTitle}>Nueva Colección</div>

                <div className={styles.field}>
                    <label className={styles.label}>Título</label>
                    <input className={styles.input} value={form.titulo} onChange={(e) => setField('titulo', e.target.value)} />
                </div>

                <div className={styles.field}>
                    <label className={styles.label}>Descripción</label>
                    <textarea className={styles.textarea} value={form.descripcion} onChange={(e) => setField('descripcion', e.target.value)} />
                </div>

                <div className={styles.field}>
                    <label className={styles.label}>Categoría</label>
                    <select className={styles.select} value={form.categoria} onChange={(e) => setField('categoria', e.target.value)}>
                        <option value="">Selecciona una categoría</option>
                        {CATEGORIAS.map((c) => <option key={c} value={c}>{c}</option>)}
                    </select>
                </div>

                <div className={styles.field}>
                    <label className={styles.label}>Algoritmo</label>
                    <select className={styles.select} value={form.algoritmo} onChange={(e) => setField('algoritmo', e.target.value)}>
                        <option value="">Selecciona un algoritmo</option>
                        {ALGORITMOS.map((a) => <option key={a} value={a}>{a}</option>)}
                    </select>
                </div>

                <div className={styles.field}>
                    <label className={styles.label}>Fuente</label>
                    <input className={styles.input} value={form.fuente} onChange={(e) => setField('fuente', e.target.value)} />
                </div>

                <div className={styles.formActions}>
                    <button className={styles.btnPrimary} onClick={guardarColeccion}>Guardar Colección</button>
                    <button className={styles.btnDanger} onClick={() => router.back()}>Cancelar</button>
                </div>
            </div>
        </div>
    );
}