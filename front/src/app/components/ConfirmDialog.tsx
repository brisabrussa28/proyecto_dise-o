"use client";
import React from 'react';
import styles from '../css/AdminConfig.module.css';

export default function ConfirmDialog({
  message,
  onConfirm,
  onCancel,
}: {
  message: string;
  onConfirm: () => void;
  onCancel: () => void;
}) {
  return (
    <div className={styles.popupOverlay}>
      <div className={styles.popupCard} role="dialog" aria-modal="true">
        <div className={styles.popupTitle}>Confirmar acción</div>
        <div className={styles.popupMsg}>{message}</div>
        <div className={styles.popupActions}>
          <button className={styles.popupBtnGhost} type="button" onClick={onCancel}>Cancelar</button>
          <button className={styles.popupBtn} type="button" onClick={onConfirm}>Sí, eliminar</button>
        </div>
      </div>
    </div>
  );
}

