"use client";
import React from 'react';
import styles from '../css/ResultsPanel.module.css';

type Item = {
  id: string;
  titulo: string;
  categoria: string;
  coleccion: string;
  fechaISO: string;
};

export default function ResultsPanel({ items, onPick, variant = 'floating', expanded = false }: { items: Item[]; onPick: (id: string) => void; variant?: 'floating' | 'inline'; expanded?: boolean }) {
  return (
    <div className={`${styles.panel} ${variant === 'inline' ? styles.inline : ''}`}>
      <div className={styles.title}>Resultados ({items.length})</div>
      <div
        className={`${styles.list} ${variant === 'inline' && items.length > 5 ? (expanded ? styles.scrollExpanded : styles.scroll) : ''}`}
      >
        {items.map((h) => (
          <div key={h.id} className={styles.itemRow}>
            <div className={styles.itemContent}>
              <div className={styles.itemTitle}>{h.titulo}</div>
              <div className={styles.itemMeta}>{h.categoria} · {h.coleccion} · {h.fechaISO}</div>
            </div>
            <button className={styles.viewBtn} onClick={() => onPick(h.id)}>Ver Info</button>
          </div>
        ))}
      </div>
      {/* Si hace falta, se puede mostrar un contador o paginación aquí. */}
    </div>
  );
}
