"use client";
import React, {Suspense, useEffect, useState} from 'react';
import {useRouter, useSearchParams} from 'next/navigation';
import {useSession} from '../../../components/SessionContext';
import styles from '../../../css/AdminConfig.module.css';
import {findFuente} from '../../../lib/configData';

function FuenteFormContent() {
    const {role, token, isAuthenticated} = useSession();
    const router = useRouter();
    const params = useSearchParams();
    const id = params.get('id');
    const isEdit = Boolean(id);
    const [nombre, setNombre] = useState('');
    const [tipoFuente, setTipoFuente] = useState('Dinamica');
    const [archivoJSON, setArchivoJson] = useState<File | null>(null);
    const [archivoCsv, setArchivoCsv] = useState<File | null>(null);


    useEffect(() => {
        if (isEdit && id) {
            const fuente = findFuente(id);
            if (fuente) setNombre(fuente.nombre);

        }
    }, [isEdit, id]);

    if (role !== 'Administrador') {
        return (
            <div className={styles.page}>
                <div className={styles.formWrap}>
                    <div className={styles.formTitle}>Fuente</div>
                    <p>Solo Administradores pueden acceder a este formulario.</p>
                </div>
            </div>
        );
    }

    const agregarFuente = async () => {
        const authToken = token;
        const formData = new FormData();

        formData.append('nombre', nombre);
        // const fechaActual = new Date();
        // const formatoFechaActual = `${fechaActual.getDate().toString().padStart(2, '0')}/${(fechaActual.getMonth() + 1).toString().padStart(2, '0')}/${fechaActual.getFullYear()}`;
        // formData.append('formatoFecha', formatoFechaActual);

        if (tipoFuente === 'Estatica') {
            formData.append('archivoJSON', archivoJSON);
        }
        if (tipoFuente === 'CSV') {
            formData.append('archivoCsv', archivoCsv);
        }

        try {
            const res = await fetch('/fuentes', {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${authToken}`,
                },
                body: formData,
            });
            if (!res.ok) throw new Error('Error al subir la fuente.');
            alert(`Fuente "${nombre}" enviada`);
            router.back();
        } catch (e) {
            console.error(e);
            alert('No se pudo conectar con el servidor');
        }
    };

    return (
        <div className={styles.page}>
            <div className={styles.formWrap}>
                <div className={styles.formTitle}>{isEdit ? 'Editar Fuente' : 'Nueva Fuente'}</div>
                <div className={styles.field}>
                    <label className={styles.label}>Tipo de Fuente</label>
                    <select
                        className={styles.select}
                        value={tipoFuente}
                        onChange={(e) => setTipoFuente(e.target.value as 'Estatica' | 'CSV')}
                    >
                        <option value="Dinamica"> Fuente Dinamica</option>
                        <option value="Estatica">Fuente Estática</option>
                        <option value="CSV">Fuente CSV</option>
                    </select>
                </div>
                {tipoFuente === 'Estatica' && (
                    <>
                        <div className={styles.field}>
                            <label className={styles.label}>Nombre</label>
                            <input
                                className={styles.input}
                                placeholder="Nombre de la fuente"
                                value={nombre}
                                onChange={(e) => setNombre(e.target.value)}
                            />
                        </div>
                        <div className={styles.field}>
                            <label className={styles.label}>Archivo JSON</label>
                            <input
                                type="file"
                                accept=".json"
                                className={styles.input}
                                onChange={(e) => {
                                    const file = e.target.files?.[0];
                                    if (file) setArchivoJson(file);
                                }}
                            />
                        </div>
                    </>
                )}
                {tipoFuente === 'Dinamica' && (
                    <>
                        <div className={styles.field}>
                            <label className={styles.label}>Nombre</label>
                            <input
                                className={styles.input}
                                placeholder="Nombre de la fuente"
                                value={nombre}
                                onChange={(e) => setNombre(e.target.value)}
                            />
                        </div>
                    </>
                )}
                {tipoFuente === 'CSV' && (
                    <>
                        <div className={styles.field}>
                            <label className={styles.label}>Nombre</label>
                            <input
                                className={styles.input}
                                placeholder="Nombre de la fuente"
                                value={nombre}
                                onChange={(e) => setNombre(e.target.value)}
                            />
                        </div>
                        <div className={styles.field}>
                            <label className={styles.label}>Archivo CSV</label>
                            <input
                                type="file"
                                accept=".csv"
                                className={styles.input}
                                onChange={(e) => {
                                    const file = e.target.files?.[0];
                                    if (file) setArchivoCsv(file);
                                }}
                            />
                        </div>
                    </>
                )}
                <div className={styles.formActions}>
                    <button
                        className={styles.btnPrimary}
                        onClick={agregarFuente}
                        disabled={!isAuthenticated || !token}
                    >
                        Guardar Fuente
                    </button>
                    <button className={styles.btnDanger} onClick={() => router.back()}>Cancelar Acción</button>
                </div>
            </div>
        </div>
    );
}

export default function FuenteFormPage() {
    return (
        <Suspense fallback={<div className={styles.page}>Cargando formulario...</div>}>
            <FuenteFormContent/>
        </Suspense>
    );
}
