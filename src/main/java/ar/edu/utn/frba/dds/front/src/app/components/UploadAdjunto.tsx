"use client";
import React, { useRef, useState } from 'react';
import styles from '../css/UploadAdjunto.module.css';

type Preview = { id: string; name: string; url: string; type: 'imagen' | 'video' };

export default function UploadAdjunto() {
  const inputRef = useRef<HTMLInputElement | null>(null);
  const [items, setItems] = useState<Preview[]>([]);

  const onFiles = (files: FileList | null) => {
    if (!files) return;
    const next: Preview[] = [];
    Array.from(files).forEach((f) => {
      const url = URL.createObjectURL(f);
      const type = f.type.startsWith('video') ? 'video' : 'imagen';
      next.push({ id: `${f.name}-${f.size}-${Date.now()}`, name: f.name, url, type });
    });
    setItems((prev) => [...prev, ...next]);
  };

  return (
    <div className={styles.wrap}>
      <label className={styles.label}>Adjuntos (imágenes o videos) — UI mock</label>
      <div
        className={styles.drop}
        onClick={() => inputRef.current?.click()}
        onDragOver={(e) => e.preventDefault()}
        onDrop={(e) => {
          e.preventDefault();
          onFiles(e.dataTransfer.files);
        }}
      >
        <p>Haz clic o arrastra archivos aquí</p>
        <input
          ref={inputRef}
          className={styles.input}
          type="file"
          multiple
          accept="image/*,video/*"
          onChange={(e) => onFiles(e.target.files)}
        />
      </div>
      {!!items.length && (
        <div className={styles.grid}>
          {items.map((i) => (
            <div key={i.id} className={styles.card}>
              {i.type === 'imagen' ? (
                // eslint-disable-next-line @next/next/no-img-element
                <img src={i.url} alt={i.name} />
              ) : (
                <video src={i.url} controls />
              )}
              <div className={styles.caption}>{i.name}</div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
