"use client";
import React, { useState } from 'react';
import { useRouter } from 'next/navigation';
import styles from '../../../css/ReportForm.module.css';

export default function ReportarPage({ params }: { params: Promise<{ id: string }> }) {
  const router = useRouter();
  const [motivo, setMotivo] = useState('');
  const minChars = 500;
  const [popup, setPopup] = useState<null | { type: 'error' | 'success'; message: string }>(null);

  // Unwrap Next.js params Promise (React 19 `use`)
  const { id } = (React as any).use ? (React as any).use(params) : (params as unknown as { id: string });

  const submit = (e: React.FormEvent) => {
    e.preventDefault();
    if (motivo.trim().length < minChars) {
      setPopup({ type: 'error', message: `El motivo debe tener al menos ${minChars} carácteres.` });
      return;
    }
    // Mock de envío
    console.log('Reporte enviado:', { hechoId: id, motivo });
    setPopup({ type: 'success', message: 'Reporte enviado correctamente. ¡Gracias por colaborar!' });
  };

  return (
    <div className={styles.page}>
      <div className={styles.card}>
        <h1 className={styles.title}>Hecho: ID {id}</h1>
        <form onSubmit={submit}>
          <div>
            <div className={styles.label}>Motivo del reporte</div>
            <textarea
              className={styles.textarea}
              placeholder="Campo obligatorio."
              value={motivo}
              onChange={(e) => setMotivo(e.target.value)}
            />
            <div className={styles.counter}>{motivo.trim().length} / {minChars}</div>
          </div>
          <p className={styles.note}>
            El reporte debe tener un mínimo de 500 carácteres explicando sus razones para eliminar el hecho, no se aceptan insultos ni vaguedades. Una vez enviado el reporte, este no se puede deshacer y será almacenado para su consecuente análisis durante los siguientes 7 días posteriores al lanzamiento del mismo. La empresa no se hace responsable ante cualquier inconveniente.
          </p>
          <div className={styles.actions}>
            <button className={styles.primary} type="submit">Enviar Reporte</button>
            <button className={styles.danger} type="button" onClick={() => router.replace('/')}>Cancelar Reporte</button>
          </div>
        </form>
      </div>
      {popup && (
        <div className={styles.popupOverlay}>
          <div className={styles.popupCard} role="dialog" aria-modal="true">
            <div className={styles.popupTitle}>{popup.type === 'error' ? 'Faltan caracteres' : 'Reporte enviado'}</div>
            <div className={styles.popupMsg}>{popup.message}</div>
            <div className={styles.popupActions}>
              {popup.type === 'error' ? (
                <button className={styles.popupBtn} type="button" onClick={() => setPopup(null)}>Entendido</button>
              ) : (
                <button className={styles.popupBtn} type="button" onClick={() => { setPopup(null); router.replace('/'); }}>OK</button>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
