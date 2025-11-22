"use client";
import React, { useState } from 'react';
import { useRouter } from 'next/navigation';
import Image from 'next/image';
import { useSession } from '../../components/SessionContext';
import styles from '../../css/AuthForm.module.css';
import mm1 from '../../assets/imgs/mm1.png';

export default function LoginPage() {
    const router = useRouter();
    const { login } = useSession();
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');

    const submit = async (e: React.FormEvent) => {
        e.preventDefault();

        try {
            const res = await fetch('http://localhost:9001/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email, password }),
                credentials: 'include', // 👈 importante para que se guarde la cookie
            });

            if (res.ok) {
                const mensaje = await res.text();
                const rolExtraido = mensaje.includes('Administrador') ? 'Administrador' : 'Contribuyente';
                login(rolExtraido, 'simulado-1234');
                router.replace('/');
            } else {
                alert('Credenciales inválidas');
            }
        } catch (err) {
            console.error('Error al iniciar sesión:', err);
            alert('Error de conexión con el servidor');
        }
    };

    const entrarComoAdmin = async () => {
        try {
            const res = await fetch('http://localhost:9001/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email: 'admin1@mock.com', password: 'admin123' }),
                credentials: 'include',
            });

            if (res.ok) {
                const mensaje = await res.text();
                const rolExtraido = mensaje.includes('Administrador') ? 'Administrador' : 'Contribuyente';
                login(rolExtraido, '1234');
                router.replace('/');
            } else {
                alert('No se pudo iniciar sesión como admin');
            }
        } catch (err) {
            console.error('Error al loguear como admin:', err);
            alert('Error de conexión con el servidor');
        }
    };

    return (
        <div className={styles.fullPage}>
            <div className={styles.wrap}>
                <div className={styles.hero}>
                    <h1 className={styles.heroTitle}>¡Nos alegra tu regreso!</h1>
                    <Image className={styles.heroImg} src={mm1} alt="Mascota MetaMapa" priority />
                </div>
                <div className={styles.sectionTitle}>Iniciar Sesión</div>
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
                        <button className={styles.btn} type="submit">Ingresar</button>
                        <button className={styles.btn} type="button" onClick={() => router.push('/auth/registro')}>No tengo cuenta</button>
                        <button className={styles.btn} type="button" onClick={() => router.replace('/')}>Invitado</button>
                    </div>
                    <div className={styles.actions}>
                        <button className={styles.btn} type="button" onClick={entrarComoAdmin}>Entrar como Admin (mock)</button>
                    </div>
                    <div className={styles.mutedLink}>
                        <a href="#">Olvidé mi contraseña</a>
                    </div>
                </form>
            </div>
        </div>
    );
}