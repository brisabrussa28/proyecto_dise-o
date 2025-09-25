package ar.edu.utn.frba.dds.domain.estadisicas;

import ar.edu.utn.frba.dds.domain.coleccion.Coleccion;
import ar.edu.utn.frba.dds.domain.exportador.Exportador;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.reportes.RepositorioDeSolicitudes;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;


public class CentralDeEstadisticas {

    private static final Logger logger = Logger.getLogger(CentralDeEstadisticas.class.getName());
    private RepositorioDeSolicitudes repo;
    private Exportador<Estadistica> exportador; // <-- CAMBIO: Agregamos el campo

    // --- CAMBIO: Nuevo método setter para inyectar la dependencia ---
    public void setExportador(Exportador<Estadistica> exportador) {
        this.exportador = exportador;
    }

    public void setRepo(RepositorioDeSolicitudes repo) {
        this.repo = repo;
    }

    public List<Hecho> getAllHechos(List<Coleccion> colecciones) {
        return colecciones.stream()
                .flatMap(lista -> lista.getHechos(repo)
                        .stream())
                .collect(Collectors.toList());
    }

    public List<Estadistica> hechosPorProvinciaDeUnaColeccion(Coleccion coleccion) {
        return coleccion.getHechos(repo)
                .stream()
                .collect(Collectors.groupingBy(Hecho::getProvincia, Collectors.counting()))
                .entrySet()
                .stream()
                .map(entry -> new Estadistica(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    public Estadistica provinciaConMasHechos(Coleccion coleccion) {
        return hechosPorProvinciaDeUnaColeccion(coleccion).stream()
                .max(Comparator.comparing(Estadistica::getValor))
                .orElse(new Estadistica("Sin Datos", 0L));
    }

    public List<Estadistica> hechosPorCategoria(List<Coleccion> colecciones) {
        return getAllHechos(colecciones).stream()
                .collect(Collectors.groupingBy(Hecho::getCategoria, Collectors.counting()))
                .entrySet()
                .stream()
                .map(entry -> new Estadistica(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    public Estadistica categoriaConMasHechos(List<Coleccion> colecciones) {
        return hechosPorCategoria(colecciones).stream()
                .max(Comparator.comparing(Estadistica::getValor))
                .orElse(null);
    }

    public List<Estadistica> hechosPorProvinciaSegunCategoria(List<Coleccion> colecciones, String categoria) {
        return getAllHechos(colecciones).stream()
                .filter(hecho -> Objects.equals(hecho.getCategoria(), categoria))
                .collect(Collectors.groupingBy(Hecho::getProvincia, Collectors.counting()))
                .entrySet()
                .stream()
                .map(entry -> new Estadistica(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    public Estadistica provinciaConMasHechosDeCiertaCategoria(List<Coleccion> colecciones, String categoria) {
        return hechosPorProvinciaSegunCategoria(colecciones, categoria).stream()
                .max(Comparator.comparing(Estadistica::getValor))
                .orElse(new Estadistica("Sin Datos", 0L));
    }

    public List<Estadistica> hechosPorHora(List<Coleccion> colecciones, String categoria) {
        return getAllHechos(colecciones).stream()
                .filter(hecho -> Objects.equals(hecho.getCategoria(), categoria))
                .collect(Collectors.groupingBy(
                        hecho -> String.format(
                                "%02d",
                                hecho.getFechasuceso()
                                        .getHour()
                        ), Collectors.counting()
                ))
                .entrySet()
                .stream()
                .map(entry -> new Estadistica(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    public Estadistica horaConMasHechosDeCiertaCategoria(List<Coleccion> colecciones, String categoria) {
        return hechosPorHora(colecciones, categoria).stream()
                .max(Comparator.comparing(Estadistica::getValor))
                .orElse(new Estadistica("Sin Datos", 0L));
    }

    public double porcentajeDeSolicitudesSpam() {
        return (double) repo.cantidadDeSpamDetectado()
                / (repo.cantidadDeSpamDetectado()
                + repo.cantidadSolicitudes())
                * 100;
    }

    public void export(List<Estadistica> datos, String rutaArchivo) {
        // --- CAMBIO: Ahora usamos el exportador que nos inyectaron ---
        if (this.exportador == null) {
            logger.severe("El exportador no ha sido configurado. No se puede exportar.");
            throw new IllegalStateException("Exportador no configurado. Use setExportador() primero.");
        }
        if (datos == null) {
            logger.warning("No se exportó a '" + rutaArchivo + "' porque la lista de datos es nula.");
            return;
        }

        List<Estadistica> datosValidos = datos.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        this.exportador.exportar(datosValidos, rutaArchivo);
    }
}

