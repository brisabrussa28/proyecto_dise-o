"use client";
import React from 'react';
import FormHecho from '../../components/FormHecho';
import { useSession } from '../../components/SessionContext';
import { useParams } from 'next/navigation';

// TODO: Implementar integración con backend en entrega 6
// TODO: Validar formularios
// TODO: Cargar datos reales desde API de MetaMapa

export default function EditarHechoPage() {
  const { role } = useSession();
  const params = useParams<{ id: string }>();
  const id = params?.id;

  const mockData = {
    titulo: 'Hecho de ejemplo ' + id,
    descripcion: 'Descripción mock para editar el hecho ' + id,
    fecha: '2024-11-20',
    categoria: 'Demostración',
    lat: '-34.6037',
    lng: '-58.3816',
    fuentes: 'https://fuente.ejemplo',
  };

  return (
    <div style={{ padding: 16 }}>
      <h2>Ver/Editar Hecho #{id}</h2>
      {role !== 'Contribuyente' && role !== 'Administrador' ? (
        <p style={{ opacity: 0.8 }}>
          Solo el Contribuyente creador o un Administrador puede editar. (UI mock)
        </p>
      ) : (
        <FormHecho initialData={mockData} />
      )}
    </div>
  );
}

