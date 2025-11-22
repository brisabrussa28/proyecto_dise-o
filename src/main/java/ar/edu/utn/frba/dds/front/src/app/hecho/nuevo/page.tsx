"use client";
import React from 'react';
import FormHecho, { HechoInput } from '../../components/FormHecho';
import { useSession } from '../../components/SessionContext';
import formStyles from '../../css/FormHecho.module.css';

export default function CrearHechoPage() {
    const { role, token } = useSession();

    const enviarHechoAlBackend = async (payload: any) => {
        try {
            const res = await fetch('http://localhost:9001/hechos', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`,
                },
                body: JSON.stringify(payload),
            });

            if (!res.ok) {
                const errorText = await res.text();
                throw new Error(`Error del servidor: ${errorText}`);
            }

            const hechoCreado = await res.json();
            alert('Hecho creado correctamente');
            console.log('Hecho creado:', hechoCreado);
        } catch (error) {
            console.error('Error al enviar el hecho:', error);
            alert('No se pudo enviar el hecho al servidor');
        }
    };

    return (
        <div className={formStyles.pageWrap}>
            <h2 className={formStyles.title}>Crear Hecho</h2>
            {role !== 'Contribuyente' && role !== 'Administrador' ? (
                <p style={{ opacity: 0.8 }}>
                    Debes iniciar sesión para crear hechos. (UI mock)
                </p>
            ) : (
                <FormHecho onSubmit={enviarHechoAlBackend} />
            )}
        </div>
    );
}
