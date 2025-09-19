package ar.edu.utn.frba.dds.domain.estadisicas;

import ar.edu.utn.frba.dds.domain.coleccion.Coleccion;
import ar.edu.utn.frba.dds.domain.exportador.csv.ExportadorCSV;
import ar.edu.utn.frba.dds.domain.exportador.csv.modoexportacion.ModoTimestamp;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.reportes.RepositorioDeSolicitudes;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class CentralDeEstadisticas {

  public void setRepo(RepositorioDeSolicitudes repo) {
    this.repo = repo;
  }

  private RepositorioDeSolicitudes repo;

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
                                                      .orElse(null);
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
                                                                   .orElse(null);
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
                                                .orElse(null);
  }

  public double porcentajeDeSolicitudesSpam() {
    return (double) repo.cantidadDeSpamDetectado()
        / (repo.cantidadDeSpamDetectado()
        + repo.cantidadSolicitudes())
        * 100;
  }

  /**
   * Exporta una lista de datos estadísticos a un archivo CSV.
   * Utiliza ExportadorCSV con ModoTimestamp para generar un archivo con un nombre único.
   *
   * @param datos       La lista de objetos Estadistica a exportar.
   * @param rutaArchivo La ruta base para el archivo de salida.
   */
  public void export(List<Estadistica> datos, String rutaArchivo) {
    if (datos == null || datos.isEmpty()) {
      return;
    }

    ExportadorCSV<Estadistica> exportador = new ExportadorCSV<>(new ModoTimestamp());
    exportador.exportar(datos, rutaArchivo);
  }
}

/*
Específicamente, se piden obtener datos que permitan responder las siguientes preguntas:
De una colección, ¿en qué provincia se agrupan la mayor cantidad de hechos reportados?
¿Cuál es la categoría con mayor cantidad de hechos reportados?
¿En qué provincia se presenta la mayor cantidad de hechos de una cierta categoría?
¿A qué hora del día ocurren la mayor cantidad de hechos de una cierta categoría?
¿Cuántas solicitudes de eliminación son spam?

* */



