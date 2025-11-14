"use client";
import { useSession } from '../../components/SessionContext';
import styles from '../../css/Stats.module.css';
import stylesSolicitudes from '../../css/Solicitudes.module.css';
import React, { useEffect, useState } from 'react';

const mockSolicitudes = [
    { id: 'S-001', tipo: 'Alta de Hecho', usuario: 'contrib1', estado: 'Pendiente' },
    { id: 'S-002', tipo: 'Edición de Hecho', usuario: 'contrib2', estado: 'Pendiente' },
    // ...otros mock
];

export default function SolicitudesPage() {
    const { role, token } = useSession();
    const [solicitudes, setSolicitudes] = useState(mockSolicitudes);

    useEffect(() => {
        const cargarSolicitudes = async () => {
            if (!token) return;

            try {
                const res = await fetch('/api/solicitudes', {
                    headers: {
                        'Authorization': `Bearer ${token}`,
                    },
                });
                if (!res.ok) throw new Error('Error al cargar solicitudes');
                const data = await res.json();
                setSolicitudes(data);
            } catch (err) {
                console.warn('Usando datos mockeados por error de conexión:', err);
                setSolicitudes(mockSolicitudes);
            }
        };

        cargarSolicitudes();
    }, [token]);

    const aprobarSolicitud = async (id: string) => {
        if (!token) {
            alert('No estás autenticado.');
            return;
        }
        try {
            const res = await fetch(`/solicitudes/${id}/aprobar`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`,
                },
            });
            if (!res.ok) throw new Error('Error al aprobar solicitud');
            alert(`Solicitud ${id} aprobada`);
        } catch (err) {
            console.error(err);
            alert('No se pudo conectar con el servidor');
        }
    };

    const marcarComoSpam = async (id: string) => {
        if (!token) {
            alert('No estás autenticado.');
            return;
        }
        try {
            const res = await fetch(`/solicitudes/${id}/spam`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`,
                },
            });
            if (!res.ok) throw new Error('Error al marcar como spam');
            alert(`Solicitud ${id} marcada como spam`);
        } catch (err) {
            console.error(err);
            alert('No se pudo conectar con el servidor');
        }
    };

    if (role !== 'Administrador') {
        return (
            <div className={styles.page}>
                <div className={styles.card}>
                    <div className={styles.title}>Solicitudes Pendientes</div>
                    <p>Solo Administradores pueden acceder a esta vista. (UI mock)</p>
                </div>
            </div>
        );
    }

    return (
        <div className={styles.page}>
            <div className={styles.card}>
                <div className={styles.title}>Solicitudes Pendientes</div>
                <ul className={`${stylesSolicitudes.contenedor} ${stylesSolicitudes.scroll}`}>
                    {solicitudes.map((s) => (
                        <li key={s.id} style={{ margin: '8px 0' }} className={stylesSolicitudes.panel}>
                            <div className={stylesSolicitudes.texto}>
                                <strong style={{ fontSize: 22, fontWeight: 700 }}>{s.id}</strong>
                                <div>{s.tipo}</div>
                                <div>Solicitante: {s.usuario}</div>
                                <div style={{ marginTop: 15 }}>Descripción: {s.estado}</div>
                            </div>
                            <div className={stylesSolicitudes.rowActions}>
                                <button
                                    className={`${stylesSolicitudes.btn} ${stylesSolicitudes.btnBlue}`}
                                    onClick={() => aprobarSolicitud(s.id)}
                                >
                                    Aprobar
                                </button>
                                <button
                                    className={`${stylesSolicitudes.btn} ${stylesSolicitudes.btnRed}`}
                                    onClick={() => marcarComoSpam(s.id)}
                                >
                                    Spam
                                </button>
                            </div>
                        </li>
                    ))}
                </ul>
            </div>
        </div>
    );
}