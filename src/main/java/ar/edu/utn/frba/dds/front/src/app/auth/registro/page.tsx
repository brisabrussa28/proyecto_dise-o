"use client";
import React, { useState } from 'react';
import { useRouter } from 'next/navigation';
import Image from 'next/image';
import { useSession } from '../../components/SessionContext';
import styles from '../../css/AuthForm.module.css';
import mm2 from '../../assets/imgs/mm2.png';

// TODO: Implementar integración con backend en entrega 6
// TODO: Validar formularios
// TODO: Cargar datos reales desde API de MetaMapa

export default function RegistroPage() {
  const router = useRouter();
  const { login } = useSession();
  const [nombre, setNombre] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');

  const submit = (e: React.FormEvent) => {
    e.preventDefault();
    login('Contribuyente');
    router.replace('/');
  };

  return (
    <div className={styles.fullPage}>
      <div className={styles.wrap}>
        <div className={styles.hero}>
          <h1 className={styles.heroTitle}>¡Se parte de nuestra comunidad!</h1>
          <Image className={styles.heroImg} src={mm2} alt="Mascota MetaMapa" priority />
        </div>
        <div className={styles.sectionTitle}>Registro</div>
      <div className={styles.title}>Registro</div>
      <form onSubmit={submit} className={styles.form}>
        <div className={styles.row}>
          <label className={styles.label}>Usuario</label>
          <input className={styles.input} placeholder="Nuevo usuario..." value={nombre} onChange={(e) => setNombre(e.target.value)} />
        </div>
        <div className={styles.row}>
          <label className={styles.label}>Mail</label>
          <input className={styles.input} placeholder="Tu mail..." value={email} onChange={(e) => setEmail(e.target.value)} />
        </div>
        <div className={styles.row}>
          <label className={styles.label}>Contraseña</label>
          <input className={styles.input} type="password" placeholder="••••••••" value={password} onChange={(e) => setPassword(e.target.value)} />
        </div>
        <div className={styles.actions}>
          <button className={styles.btn} type="submit">Crear cuenta (mock)</button>
          <button className={styles.btn} type="button" onClick={() => router.push('/auth/login')}>Ya tengo cuenta</button>
        </div>
      </form>
      </div>
    </div>
  );
}
