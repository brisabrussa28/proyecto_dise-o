"use client";
import React, { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useSession } from '../../components/SessionContext';
import styles from '../../css/AdminConfig.module.css';
import ConfirmDialog from '../../components/ConfirmDialog';
import { MOCK_COLECCIONES, MOCK_FUENTES } from '../../lib/configData';

export default function ConfiguracionPage() {
    const { role } = useSession();
    const router = useRouter();
    const [fuentes, setFuentes] = useState(MOCK_FUENTES);
    const [colecciones, setColecciones] = useState(MOCK_COLECCIONES);
    const [confirm, setConfirm] = useState<null | { tipo: 'fuente' | 'coleccion'; id: string; nombre: string }>(null);

    useEffect(() => {
        const cargarDatos = async () => {
            try {
                const [resFuentes, resColecciones] = await Promise.all([
                    fetch('/api/fuentes'),
                    fetch('/api/colecciones'),
                ]);

                if (!resFuentes.ok || !resColecciones.ok) throw new Error('Error al cargar datos');

                const fuentesData = await resFuentes.json();
                const coleccionesData = await resColecciones.json();

                setFuentes(fuentesData);
                setColecciones(coleccionesData);
            } catch (err) {
                console.warn('Usando datos mockeados por error de conexión:', err);
                setFuentes(MOCK_FUENTES);
                setColecciones(MOCK_COLECCIONES);
            }
        };

        cargarDatos();
    }, []);

    const solicitarEliminar = (tipo: 'fuente' | 'coleccion', id: string, nombre: string) => {
        setConfirm({ tipo, id, nombre });
    };

    const eliminar = () => {
        if (!confirm) return;
        if (confirm.tipo === 'fuente') {
            setFuentes((prev) => prev.filter((f) => f.id !== confirm.id));
        } else {
            setColecciones((prev) => prev.filter((c) => c.id !== confirm.id));
        }
        setConfirm(null);
    };

    if (role !== 'Administrador') {
        return (
            <div className={styles.page}>
                <div className={styles.card}>
                    <div className={styles.title}>Fuentes y Colecciones</div>
                    <p>Solo Administradores pueden acceder a esta vista. (UI mock)</p>
                </div>
            </div>
        );
    }

    return (
        <div className={styles.page}>
            <div className={styles.card}>
                <div className={styles.title}>Administrar Fuentes y Colecciones</div>
                <div className={styles.columns}>
                    <section className={styles.panel}>
                        <div className={styles.panelTitle}>Fuentes</div>
                        <div className={styles.list}>
                            {fuentes.length === 0 && <div className={styles.empty}>No hay fuentes registradas.</div>}
                            {fuentes.map((f) => (
                                <div key={f.id} className={styles.row}>
                                    <span className={styles.rowName}>{f.nombre}</span>
                                    <div className={styles.rowActions}>
                                        <button className={`${styles.btn} ${styles.btnRed}`} onClick={() => solicitarEliminar('fuente', f.id, f.nombre)}>
                                            Eliminar
                                        </button>
                                    </div>
                                </div>
                            ))}
                        </div>
                        <button className={styles.btnAdd} onClick={() => router.push('/admin/fuentes/form')}>Agregar Fuente</button>
                    </section>

                    <section className={styles.panel}>
                        <div className={styles.panelTitle}>Colecciones</div>
                        <div className={styles.list}>
                            {colecciones.length === 0 && <div className={styles.empty}>No hay colecciones registradas.</div>}
                            {colecciones.map((c) => (
                                <div key={c.id} className={styles.row}>
                                    <span className={styles.rowName}>{c.nombre}</span>
                                    <div className={styles.rowActions}>
                                        <button className={`${styles.btn} ${styles.btnBlue}`} onClick={() => router.push(`/admin/colecciones/${c.id}/form`)}>
                                            Editar
                                        </button>
                                        <button className={`${styles.btn} ${styles.btnRed}`} onClick={() => solicitarEliminar('coleccion', c.id, c.nombre)}>
                                            Eliminar
                                        </button>
                                    </div>
                                </div>
                            ))}
                        </div>
                        <button className={styles.btnAdd} onClick={() => router.push('/admin/colecciones/form')}>Agregar Coleccion</button>
                    </section>
                </div>
            </div>

            {confirm && (
                <ConfirmDialog
                    message={`Eliminar ${confirm.tipo === 'fuente' ? 'la fuente' : 'la coleccion'} "${confirm.nombre}"? Esta acción no se puede deshacer.`}
                    onCancel={() => setConfirm(null)}
                    onConfirm={eliminar}
                />
            )}
        </div>
    );
}