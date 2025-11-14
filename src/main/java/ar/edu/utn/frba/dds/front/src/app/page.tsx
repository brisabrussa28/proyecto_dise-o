'use client';
import React, { useMemo, useRef, useState, useEffect } from 'react';
import { useSearchParams } from 'next/navigation';
import { FaEye, FaEyeSlash } from 'react-icons/fa';
import MapCanvas from './components/Map/mapCanvas';
import Image from 'next/image';
import metamapa from './assets/imgs/metamapa.png';
import HechoDetalleModal from './components/HechoDetalleModal';
import Filters, { FiltersState } from './components/Filters';
import ResultsPanel from './components/ResultsPanel';
import styles from './css/Home.module.css';
import { MOCK_HECHOS } from './components/Map/dataMocks';

export type HechoFeature = {
    id: string;
    titulo: string;
    descripcion: string;
    fechaISO: string;
    categoria: string;
    fuente: string;
    coleccion: string;
    coords: { lat: number; lng: number };
    estado: string;
    autor: string;
    adjuntos?: Array<{ id: string; url: string; tipo: 'imagen' | 'video' }>;
};

export default function Home() {
    const params = useSearchParams();

    const filters = useMemo<FiltersState>(() => ({
        categoria: params.get('categoria') || 'Todas',
        fuente: params.get('fuente') || 'Todas',
        coleccion: params.get('coleccion') || 'Todas',
        desde: params.get('desde') || '',
        hasta: params.get('hasta') || '',
    }), [params]);

    const [hechos, setHechos] = useState<HechoFeature[]>([]);
    const [showModal, setShowModal] = useState(false);
    const [seleccionado, setSeleccionado] = useState<HechoFeature | null>(null);
    const [search, setSearch] = useState('');
    const [filtersOpen, setFiltersOpen] = useState(false);
    const [filtersVisible, setFiltersVisible] = useState(true);
    const [sideWidth, setSideWidth] = useState<number>(360);
    const draggingRef = useRef(false);
    const startXRef = useRef(0);
    const startWidthRef = useRef(360);
    const mapRef = useRef<any>(null);
    const [errorMostrado, setErrorMostrado] = useState(false);

    const toggleFilters = () => {
        setFiltersOpen((prev) => {
            const next = !prev;
            setTimeout(() => {
                mapRef.current?.getMap?.()?.resize();
            }, 300);
            return next;
        });
    };

    //AUN NO FUNCIONA EL ZOOM
    const zoomIn = () => {
        const map = mapRef.current?.getMap?.();
        if (!map) return;
        map.zoomTo(map.getZoom() + 1, { duration: 300 });
    };
    const zoomOut = () => {
        const map = mapRef.current?.getMap?.();
        if (!map) return;
        map.zoomTo(map.getZoom() - 1, { duration: 300 });
    };

    const aplicarFiltros = (hechos: HechoFeature[], filtros: FiltersState) => {
        return hechos.filter((h) => {
            const coincideCategoria = filtros.categoria === 'Todas' || h.categoria === filtros.categoria;
            const coincideFuente = filtros.fuente === 'Todas' || h.fuente === filtros.fuente;
            const coincideColeccion = filtros.coleccion === 'Todas' || h.coleccion === filtros.coleccion;
            const coincideDesde = !filtros.desde || h.fechaISO >= filtros.desde;
            const coincideHasta = !filtros.hasta || h.fechaISO <= filtros.hasta;
            return coincideCategoria && coincideFuente && coincideColeccion && coincideDesde && coincideHasta;
        });
    };

    useEffect(() => {
        const onMove = (e: MouseEvent) => {
            if (!draggingRef.current) return;
            const delta = e.clientX - startXRef.current;
            const next = Math.max(320, startWidthRef.current + delta);
            setSideWidth(next);
        };
        const onUp = () => { draggingRef.current = false; };
        window.addEventListener('mousemove', onMove);
        window.addEventListener('mouseup', onUp);
        return () => {
            window.removeEventListener('mousemove', onMove);
            window.removeEventListener('mouseup', onUp);
        };
    }, []);

    useEffect(() => {
        const query = new URLSearchParams();
        if (filters.categoria !== 'Todas') query.set('categoria', filters.categoria);
        if (filters.fuente !== 'Todas') query.set('fuente', filters.fuente);
        if (filters.coleccion !== 'Todas') query.set('coleccion', filters.coleccion);
        if (filters.desde) query.set('desde', filters.desde);
        if (filters.hasta) query.set('hasta', filters.hasta);

        //CAMBIAR POR LA RUTA REAL --esto ocurre en cada fetch
        const url = `miRuta?${query.toString()}`;
        console.log('Consultando:', url);

        fetch(url)
            .then((res) => {
                if (!res.ok) throw new Error('Respuesta no válida');
                return res.json();
            })
            .then((data) => {
                console.log('Datos reales:', data);
                setHechos(data);
            })
            .catch((err) => {
                console.warn('Usando datos mockeados por error de conexión:', err);
                setHechos(aplicarFiltros(MOCK_HECHOS, filters));
                if (!errorMostrado) {
                    alert('No se pudo conectar con el servidor. Se están mostrando datos simulados.');
                    setErrorMostrado(true);
                }
            });
    }, [filters]);

    const filtrados = useMemo(() => {
        const term = search.trim().toLowerCase();
        return hechos.filter((h) =>
            !term || h.titulo.toLowerCase().includes(term)
        );
    }, [hechos, search]);

    const hechoSeleccionado = seleccionado
        ? {
            id: seleccionado.id,
            titulo: seleccionado.titulo,
            descripcion: seleccionado.descripcion,
            fecha: seleccionado.fechaISO,
            categoria: seleccionado.categoria,
            ubicacion: seleccionado.coords,
            adjuntos: seleccionado.adjuntos || [],
        }
        : null;

    return (
        <div className={styles.container}>
            {filtersOpen && (
                <aside className={styles.side} style={{ width: sideWidth }}>
                    <div className={styles.headerRow}>
                        <div className={styles.sectionTitle}>Filtros</div>
                        <button
                            className={`${styles.toggle} ${filtersVisible ? styles.toggleActive : ''}`}
                            onClick={() => setFiltersVisible((v) => !v)}
                            aria-label={filtersVisible ? 'Ocultar filtros' : 'Mostrar filtros'}
                            title={filtersVisible ? 'Ocultar filtros' : 'Mostrar filtros'}
                        >
                            {filtersVisible ? <FaEye /> : <FaEyeSlash />}
                        </button>
                    </div>

                    {filtersVisible && (
                        <Filters  resultados={filtrados.length} variant="inline" />
                    )}

                    <div className={styles.sectionTitle}>Hechos</div>
                    <ResultsPanel
                        items={filtrados}
                        onPick={(id) => {
                            const f = filtrados.find((x) => x.id === id) || null;
                            if (f) {
                                setSeleccionado(f);
                                setShowModal(true);
                            }
                        }}
                        variant="inline"
                        expanded={!filtersVisible}
                    />
                </aside>
            )}

            <div
                className={styles.resizer}
                onMouseDown={(e) => {
                    draggingRef.current = true;
                    startXRef.current = e.clientX;
                    startWidthRef.current = sideWidth;
                }}
                aria-label="Redimensionar panel"
                role="separator"
            />

            <section className={styles.map}>
                <MapCanvas
                    ref={mapRef}
                    //aparece un error porque tengo dos veces los hechoFeature, cuando eliminemos el mock se
                    // soluciona, tambien podemos unificarlos y listo, si queremos mantener los mocks
                    features={filtrados}
                    search={search}
                    onSearchChange={setSearch}
                    onFeatureClick={(f) => {
                        setSeleccionado(f);
                        setShowModal(true);
                    }}
                    onToggleFilters={toggleFilters}
                />
                <div className={`${styles.mapBrand} ${styles.brandLeft}`} aria-hidden>
                    <Image className={styles.mapBrandImg} src={metamapa} alt="MetaMapa" priority />
                </div>
            </section>

            <div style={{
                position: 'absolute',
                bottom: '12px',
                right: '12px',
                zIndex: 10,
                display: 'flex',
                flexDirection: 'column',
                gap: '10px'
            }}>
                <button className='ajuste' onClick={zoomIn}>+</button>
                <button className='ajuste' onClick={zoomOut}>−</button>
            </div>

            <HechoDetalleModal
                open={showModal}
                onClose={() => setShowModal(false)}
                hecho={hechoSeleccionado}
                onViewOnMap={() => {
                    const coords = hechoSeleccionado?.ubicacion;
                    if (coords) {
                        mapRef.current?.flyTo?.(coords);
                        setShowModal(false);
                    }
                }}
            />
        </div>
    );
}
