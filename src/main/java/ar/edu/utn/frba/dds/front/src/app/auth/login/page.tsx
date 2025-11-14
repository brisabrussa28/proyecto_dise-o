"use client";
import React, { useState } from 'react';
import { useRouter } from 'next/navigation';
import Image from 'next/image';
import { useSession } from '../../components/SessionContext';
import styles from '../../css/AuthForm.module.css';
import mm1 from '../../assets/imgs/mm1.png';

// TODO: Implementar integración con backend en entrega 6
// TODO: Validar formularios
// TODO: Cargar datos reales desde API de MetaMapa

export default function LoginPage() {
  const router = useRouter();
  const { login, token} = useSession();
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
          <h1 className={styles.heroTitle}>¡Nos alegra tu regreso!</h1>
          <Image className={styles.heroImg} src={mm1} alt="Mascota MetaMapa" priority />
        </div>
        <div className={styles.sectionTitle}>Iniciar Sesión</div>
      <div className={styles.title}>Iniciar Sesión</div>
      <form onSubmit={submit} className={styles.form}>
        <div className={styles.row}>
          <label className={styles.label}>Mail</label>
          <input className={styles.input} placeholder="Tu mail..." value={email} onChange={(e) => setEmail(e.target.value)} />
        </div>
        <div className={styles.row}>
          <label className={styles.label}>Contraseña</label>
          <input className={styles.input} placeholder="••••••••" type="password" value={password} onChange={(e) => setPassword(e.target.value)} />
        </div>
        <div className={styles.actions}>
          <button className={styles.btn} type="submit">Ingresar (mock)</button>
          <button className={styles.btn} type="button" onClick={() => router.push('/auth/registro')}>No tengo cuenta</button>
          <button className={styles.btn} type="button" onClick={() => { router.replace('/'); }}>Invitado</button>
        </div>
        <div className={styles.actions}>
          <button className={styles.btn} type="button" onClick={() => { login('Administrador', 'mock-token-123'); router.replace('/'); }}>Entrar como Admin (mock)</button>
        </div>
        <div className={styles.mutedLink}>
          <a href="#">Olvidé mi contraseña</a>
        </div>
      </form>
      </div>
    </div>
  );
}
