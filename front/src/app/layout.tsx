import React from 'react';
import './css/globals.css';
import Toolbar from './components/Toolbar';
import { SessionProvider } from './components/SessionContext';

export const metadata = {
  title: 'MetaMapa',
  description: 'Plataforma para visualizar hechos geográficos',
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="es">
      <body>
        <SessionProvider>
          <Toolbar />
          <main className="app-container">{children}</main>
        </SessionProvider>
      </body>
    </html>
  );
}
