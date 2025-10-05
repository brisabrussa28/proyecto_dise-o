package ar.edu.utn.frba.dds.domain.estadisticas;

import ar.edu.utn.frba.dds.domain.coleccion.Coleccion;
import ar.edu.utn.frba.dds.domain.exportador.Exportador;
import ar.edu.utn.frba.dds.domain.filtro.Filtro;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.reportes.GestorDeSolicitudes;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Clase responsable de calcular y exportar estadísticas.
 * Debe ser configurada con un GestorDeSolicitudes para poder excluir
 * los hechos eliminados de los cálculos.
 */
public class CentralDeEstadisticas {

  private static final Logger logger = Logger.getLogger(CentralDeEstadisticas.class.getName());
  private GestorDeSolicitudes gestor; // AHORA: Depende del Gestor
  private Exportador<Estadistica> exportador;
  private Filtro filtroAdicional; // Filtro opcional para refinar estadísticas

  // --- Lógica Principal de Estadísticas ---

  public List<Estadistica> hechosPorProvinciaDeUnaColeccion(Coleccion coleccion) {
    Filtro filtroExcluyente = obtenerFiltroExcluyente();
    List<Hecho> hechosFiltrados = coleccion.obtenerHechosFiltrados(filtroExcluyente);

    // Se aplica el filtro adicional si fue configurado
    if (this.filtroAdicional != null) {
      hechosFiltrados = this.filtroAdicional.filtrar(hechosFiltrados);
    }

    return hechosFiltrados.stream()
                          .collect(Collectors.groupingBy(
                              Hecho::getProvincia,
                              Collectors.counting()
                          ))
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
    List<Hecho> todosLosHechos = obtenerTodosLosHechos(colecciones);
    return todosLosHechos.stream()
                         .collect(Collectors.groupingBy(Hecho::getCategoria, Collectors.counting()))
                         .entrySet()
                         .stream()
                         .map(entry -> new Estadistica(entry.getKey(), entry.getValue()))
                         .collect(Collectors.toList());
  }

  public Estadistica categoriaConMasHechos(List<Coleccion> colecciones) {
    return hechosPorCategoria(colecciones).stream()
                                          .max(Comparator.comparing(Estadistica::getValor))
                                          .orElse(new Estadistica("Sin Datos", 0L));
  }

  public List<Estadistica> hechosPorProvinciaSegunCategoria(
      List<Coleccion> colecciones, String categoria) {
    List<Hecho> todosLosHechos = obtenerTodosLosHechos(colecciones);
    return todosLosHechos.stream()
                         .filter(hecho -> Objects.equals(hecho.getCategoria(), categoria))
                         .collect(Collectors.groupingBy(Hecho::getProvincia, Collectors.counting()))
                         .entrySet()
                         .stream()
                         .map(entry -> new Estadistica(entry.getKey(), entry.getValue()))
                         .collect(Collectors.toList());
  }

  public Estadistica provinciaConMasHechosDeCiertaCategoria(
      List<Coleccion> colecciones, String categoria) {
    return hechosPorProvinciaSegunCategoria(colecciones, categoria).stream()
                                                                   .max(Comparator.comparing(Estadistica::getValor))
                                                                   .orElse(new Estadistica("Sin Datos", 0L));
  }

  public List<Estadistica> hechosPorHora(List<Coleccion> colecciones, String categoria) {
    List<Hecho> todosLosHechos = obtenerTodosLosHechos(colecciones);
    return todosLosHechos.stream()
                         .filter(hecho -> Objects.equals(hecho.getCategoria(), categoria))
                         .collect(Collectors.groupingBy(
                             hecho -> String.format("%02d", hecho.getFechasuceso().getHour()),
                             Collectors.counting()
                         ))
                         .entrySet()
                         .stream()
                         .map(entry -> new Estadistica(entry.getKey(), entry.getValue()))
                         .collect(Collectors.toList());
  }

  public Estadistica horaConMasHechosDeCiertaCategoria(
      List<Coleccion> colecciones, String categoria) {
    return hechosPorHora(colecciones, categoria).stream()
                                                .max(Comparator.comparing(Estadistica::getValor))
                                                .orElse(new Estadistica("Sin Datos", 0L));
  }

  public long cantidadDeSolicitudesSpam() {
    validarGestorConfigurado();
    return gestor.cantidadDeSpamDetectado();
  }

  // --- Lógica de Exportación ---

  public void exportar(List<Estadistica> datos, String rutaArchivo) {
    validarExportadorConfigurado();
    if (datos == null || datos.isEmpty()) {
      logger.warning("No se exportó a '" + rutaArchivo + "' porque no había datos para exportar.");
      return;
    }

    List<Estadistica> datosValidos = datos.stream()
                                          .filter(Objects::nonNull)
                                          .collect(Collectors.toList());

    this.exportador.exportar(datosValidos, rutaArchivo);
  }

  // --- Métodos Auxiliares y de Configuración ---

  public void setExportador(Exportador<Estadistica> exportador) {
    this.exportador = exportador;
  }

  public void setGestor(GestorDeSolicitudes gestor) {
    this.gestor = gestor;
  }

  public void setFiltroAdicional(Filtro filtro) {
    this.filtroAdicional = filtro;
  }

  private List<Hecho> obtenerTodosLosHechos(List<Coleccion> colecciones) {
    Filtro filtroExcluyente = obtenerFiltroExcluyente();
    List<Hecho> todosLosHechos = colecciones.stream()
                                            .flatMap(coleccion -> coleccion.obtenerHechosFiltrados(filtroExcluyente).stream())
                                            .collect(Collectors.toList());

    if (this.filtroAdicional != null) {
      todosLosHechos = this.filtroAdicional.filtrar(todosLosHechos);
    }
    return todosLosHechos;
  }

  private Filtro obtenerFiltroExcluyente() {
    validarGestorConfigurado();
    return gestor.filtroExcluyenteDeHechosEliminados();
  }

  private void validarGestorConfigurado() {
    if (this.gestor == null) {
      throw new IllegalStateException("El gestor no ha sido configurado. Use setGestor() primero.");
    }
  }

  private void validarExportadorConfigurado() {
    if (this.exportador == null) {
      throw new IllegalStateException("El exportador no ha sido configurado. Use setExportador() primero.");
    }
  }
}
