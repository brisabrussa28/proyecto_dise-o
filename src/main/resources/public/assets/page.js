'use client'
import React from "react";
import MapCanvas from "./components/Map/mapCanvas";
import './inicio.css'
import { useRouter } from "next/navigation";
import { FaRegBell, FaRegMap, FaRegEdit, FaRegChartBar } from "react-icons/fa";
import { VscTools } from "react-icons/vsc";


export default function Home() {
  const router = useRouter();
  return (
    <div className="division">

      <div className="tool-bar">
        <div className="icon">
          <button className="meta-Mapa" onClick={() => router.replace('/')} title="Inicio"><FaRegMap></FaRegMap></button>
          <button className="hecho" title="Crear Hecho"><FaRegEdit></FaRegEdit></button>
        </div>
        <div className="options">
          <button className="notification" title="Solicitudes Pendientes"><FaRegBell></FaRegBell></button>
          <button className="fuentes-colecciones" title="Administrar Fuentes/Colecciones"><VscTools></VscTools></button>
          <button className="stats" title="Estadísticas"><FaRegChartBar></FaRegChartBar></button>
        </div>
      </div>

      <div className="Mapa">
        <MapCanvas></MapCanvas>
      </div>
    
    </div>
  );
}























































































/*
      <footer className={styles.footer}>
        <a
          href="https://nextjs.org/learn?utm_source=create-next-app&utm_medium=appdir-template&utm_campaign=create-next-app"
          target="_blank"
          rel="noopener noreferrer"
        >
          <Image
            aria-hidden
            src="/file.svg"
            alt="File icon"
            width={16}
            height={16}
          />
          Learn
        </a>
        <a
          href="https://vercel.com/templates?framework=next.js&utm_source=create-next-app&utm_medium=appdir-template&utm_campaign=create-next-app"
          target="_blank"
          rel="noopener noreferrer"
        >
          <Image
            aria-hidden
            src="/window.svg"
            alt="Window icon"
            width={16}
            height={16}
          />
          Examples
        </a>
        <a
          href="https://nextjs.org?utm_source=create-next-app&utm_medium=appdir-template&utm_campaign=create-next-app"
          target="_blank"
          rel="noopener noreferrer"
        >
          <Image
            aria-hidden
            src="/globe.svg"
            alt="Globe icon"
            width={16}
            height={16}
          />
          Go to nextjs.org →
        </a>
      </footer>
*/
