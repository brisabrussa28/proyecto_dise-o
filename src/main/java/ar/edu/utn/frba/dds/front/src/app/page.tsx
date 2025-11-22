'use client';
import React, {useMemo, useRef, useState, useEffect} from 'react';
import {useSearchParams} from 'next/navigation';
import {FaEye, FaEyeSlash} from 'react-icons/fa';
import MapCanvas from './components/Map/mapCanvas';
import Image from 'next/image';
import metamapa from './assets/imgs/metamapa.png';
import HechoDetalleModal from './components/HechoDetalleModal';
import Filters, {FiltersState} from './components/Filters';
import ResultsPanel from './components/ResultsPanel';
import styles from './css/Home.module.css';
import api from "../lib/api";

export type HechoFeature = {
    id: string;
    titulo: string;
    descripcion: string;
    fechaISO: string;
    categoria: string;
    fuente: string;
    origen: string;
    direccion: string;
    coords: { lat: number; lng: number };
    estado: string;
    autor: string;
    adjuntos?: Array<{ id: string; url: string; }>;
    coleccion: string;
};

type Item = HechoFeature & { coleccion: string };

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
    const [colecciones, setColecciones] = useState<Array<{ nombre: string; fuentes: string[] }>>([]);
    const coleccionPorFuenteRef = useRef<Record<string, string>>({});
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

    const aplicarFiltros = (hechos: HechoFeature[], filtros: FiltersState) => {
        return hechos.filter((h) => {
            const coincideCategoria = filtros.categoria === 'Todas' || h.categoria === filtros.categoria;
            const coincideFuente = filtros.fuente === 'Todas' || h.fuente === filtros.fuente;
            const fuenteColeccion = coleccionPorFuenteRef.current[h.fuente];
            const coincideColeccion = filtros.coleccion === 'Todas' || fuenteColeccion === filtros.coleccion;
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
        const onUp = () => {
            draggingRef.current = false;
        };
        window.addEventListener('mousemove', onMove);
        window.addEventListener('mouseup', onUp);
        return () => {
            window.removeEventListener('mousemove', onMove);
            window.removeEventListener('mouseup', onUp);
        };
    }, []);

    useEffect(() => {
        const url = "http://localhost:9001";

        // Fetch collections to build fuente -> coleccion map
        fetch(`${url}/colecciones`)
            .then((res) => {
                if (!res.ok) throw new Error('No collections');
                return res.json();
            })
            .then((data: any[]) => {
                setColecciones(data || []);
                const map: Record<string, string> = {};
                (data || []).forEach((c: any) => {
                    const nombre = c.nombre || c.id || String(c);
                    const fuentes: string[] = c.fuentes || [];
                    fuentes.forEach((f) => {
                        map[f] = nombre;
                    });
                });
                coleccionPorFuenteRef.current = map;
            })
            .catch(() => {
                coleccionPorFuenteRef.current = {};
            }).finally(() => {
            fetch(url)
                .then((res) => {
                    if (!res.ok) throw new Error('Respuesta no válida');
                    return res.json();
                })
                .then((data) => {
                    console.log('Datos reales:', data);
                    const hechosMapeados: HechoFeature[] = data.map((apiHecho: any) => {
                        return {
                            id: String(apiHecho.id),
                            titulo: apiHecho.hecho_titulo,
                            descripcion: apiHecho.hecho_descripcion,
                            fechaISO: apiHecho.hecho_fecha_suceso,
                            categoria: apiHecho.hecho_categoria,
                            fuente: apiHecho.hecho_fuente,
                            origen: apiHecho.hecho_origen,
                            coords: {
                                lat: apiHecho.hecho_ubicacion.latitud,
                                lng: apiHecho.hecho_ubicacion.longitud,
                            },
                            direccion: apiHecho.hecho_direccion,
                            adjuntos: apiHecho.hecho_fotos.map((foto: any) => ({
                                id: foto.id,
                                url: foto.url
                            })),

                        }
                    })
                    console.log("Datos mapeados: ", hechosMapeados)
                    setHechos(hechosMapeados);
                })
                .catch((err) => {
                    console.warn('Usando datos mockeados por error de conexión:', err);
                    // setHechos(aplicarFiltros(MOCK_HECHOS, filters));
                    if (!errorMostrado) {
                        alert('No se pudo conectar con el servidor. Se están mostrando datos simulados.');
                        setErrorMostrado(true);
                    }
                });
        })


        const query = new URLSearchParams();
        if (filters.categoria !== 'Todas') query.set('categoria', filters.categoria);
        if (filters.fuente !== 'Todas') query.set('fuente', filters.fuente);
        if (filters.coleccion !== 'Todas') query.set('coleccion', filters.coleccion);
        if (filters.desde) query.set('desde', filters.desde);
        if (filters.hasta) query.set('hasta', filters.hasta);

    }, [filters]);

    const filtrados = useMemo(() => {
        const term = search.trim().toLowerCase();
        const conFiltros = aplicarFiltros(hechos, filters);
        return conFiltros.filter((h) =>
            !term || h.titulo.toLowerCase().includes(term)
        );
    }, [hechos, search, filters]);

    const hechoSeleccionado = seleccionado
        ? {
            id: seleccionado.id,
            titulo: seleccionado.titulo,
            descripcion: seleccionado.descripcion,
            direccion: seleccionado.direccion,
            fecha: seleccionado.fechaISO,
            categoria: seleccionado.categoria,
            ubicacion: seleccionado.coords,
            adjuntos: seleccionado.adjuntos || [],
        }
        : null;

    return (
        <div className={styles.container}>
            {filtersOpen && (
                <aside className={styles.side} style={{width: sideWidth}}>
                    <div className={styles.headerRow}>
                        <div className={styles.sectionTitle}>Filtros</div>
                        <button
                            className={`${styles.toggle} ${filtersVisible ? styles.toggleActive : ''}`}
                            onClick={() => setFiltersVisible((v) => !v)}
                            aria-label={filtersVisible ? 'Ocultar filtros' : 'Mostrar filtros'}
                            title={filtersVisible ? 'Ocultar filtros' : 'Mostrar filtros'}
                        >
                            {filtersVisible ? <FaEye/> : <FaEyeSlash/>}
                        </button>
                    </div>

                    {filtersVisible && (
                        <Filters resultados={filtrados.length} variant="inline"/>
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
                    <Image className={styles.mapBrandImg} src={metamapa} alt="MetaMapa" priority/>
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
