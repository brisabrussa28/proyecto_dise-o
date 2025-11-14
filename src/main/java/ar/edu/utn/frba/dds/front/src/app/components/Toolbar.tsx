"use client";
import React, { useEffect, useRef, useState } from 'react';
import { useRouter, usePathname } from 'next/navigation';
import { FaRegBell, FaRegMap, FaRegEdit, FaRegChartBar, FaSignInAlt, FaUserPlus } from 'react-icons/fa';
import styles from '../css/Toolbar.module.css';
import { useSession } from './SessionContext';
import Image from 'next/image';
import defaultUser from '../assets/imgs/defaultUser.png';

export default function Toolbar() {
  const router = useRouter();
  const pathname = usePathname();
  const { role, isAuthenticated, logout } = useSession();
  const [menuOpen, setMenuOpen] = useState(false);
  const userRef = useRef<HTMLDivElement | null>(null);
  const pendingCount = 3; // mock; reemplazar luego por valor real
  const pendLabel = pendingCount > 99 ? '+99' : String(pendingCount);

  useEffect(() => {
    const onClickDoc = (e: MouseEvent) => {
      if (!userRef.current) return;
      if (!userRef.current.contains(e.target as Node)) setMenuOpen(false);
    };
    document.addEventListener('mousedown', onClickDoc);
    return () => document.removeEventListener('mousedown', onClickDoc);
  }, []);

  return (
    <div className={styles.toolbar}>
      <div className={styles.left}>
        {pathname !== '/' && (
          <button className={styles.icon} onClick={() => router.replace('/')} title="Inicio">
            <FaRegMap />
          </button>
        )}
        {isAuthenticated && (role === 'Contribuyente' || role === 'Administrador') && pathname !== '/hecho/nuevo' && (
          <button className={styles.icon} onClick={() => router.push('/hecho/nuevo')} title="Crear Hecho">
            <FaRegEdit />
          </button>
        )}
      </div>
      <div className={styles.center}>
        <span onClick={() => router.replace('/')} style={{ cursor: 'pointer' }}>
          MetaMapa
        </span>
      </div>
      <div className={styles.right}>
        {/* {isAuthenticated && role === 'Administrador' && (
          <>
            <button className={styles.icon} onClick={() => router.push('/admin/solicitudes')} title="Solicitudes Pendientes">
              <FaRegBell />
            </button>
            <button className={styles.icon} onClick={() => router.push('/admin/configuracion')} title="Fuentes y Colecciones">
              <VscTools />
            </button>
            <button className={styles.icon} onClick={() => router.push('/admin/estadisticas')} title="Estadísticas">
              <FaRegChartBar />
            </button>
          </>
        )} */}

        {!isAuthenticated && (
          <>
            <button className={styles.textBtn} onClick={() => router.push('/auth/login')} title="Iniciar sesión">
              <FaSignInAlt />&nbsp;Ingresar
            </button>
            <button className={styles.textBtn} onClick={() => router.push('/auth/registro')} title="Registrarse">
              <FaUserPlus />&nbsp;Registro
            </button>
          </>
        )}

        {isAuthenticated && (
          <div className={styles.user} ref={userRef}>
            <button className={styles.avatarBtn} onClick={() => setMenuOpen((v) => !v)} aria-haspopup="menu" aria-expanded={menuOpen}>
              <Image className={styles.avatarImg} src={defaultUser} alt="Usuario" />
            </button>
            {menuOpen && (
              <div className={styles.menu} role="menu">
                <button className={styles.menuItem} onClick={() => { setMenuOpen(false); }}>Mi usuario</button>
                <button className={styles.menuItem} onClick={() => { setMenuOpen(false); router.push('/admin/configuracion'); }}>Configuración</button>
                {role === 'Administrador' && (
                  <>
                    <button className={styles.menuItem} onClick={() => { setMenuOpen(false); router.push('/admin/solicitudes'); }}>Solicitudes Pendientes ({pendLabel})</button>
                    <button className={styles.menuItem} onClick={() => { setMenuOpen(false); router.push('/admin/configuracion'); }}>Administrar Fuentes y Colecciones</button>
                    <button className={styles.menuItem} onClick={() => { setMenuOpen(false); router.push('/admin/estadisticas'); }}>Estadísticas</button>
                    <div className={styles.divider} />
                  </>
                )}
                <button className={styles.menuItem} onClick={() => { setMenuOpen(false); logout(); }}>Salir</button>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
}

