package ar.edu.utn.frba.dds.model.estadisticas;

import ar.edu.utn.frba.dds.model.coleccion.Coleccion;
import ar.edu.utn.frba.dds.model.exportador.Exportador;
import ar.edu.utn.frba.dds.model.filtro.Filtro;
import ar.edu.utn.frba.dds.model.hecho.Hecho;
import ar.edu.utn.frba.dds.model.reportes.GestorDeSolicitudes;
import ar.edu.utn.frba.dds.model.reportes.Solicitud;
import ar.edu.utn.frba.dds.repositories.ColeccionRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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

  //Cantidad de hechos por provincia segun categoria
  public List<Estadistica> hechosPorProvinciaSegunCategoria(String categoria) {
    List<Coleccion> colecciones = ColeccionRepository.instance().findAll();
    List<Hecho> todosLosHechos = obtenerTodosLosHechos(colecciones);

    List<Hecho> hechosFiltrados = todosLosHechos.stream()
                                                .filter(h -> categoria.equals(h.getCategoria()))
                                                .collect(Collectors.toList());

    Map<String, Long> cantidadPorProvincia = hechosFiltrados.stream()
                                                            .collect(Collectors.groupingBy(
                                                                Hecho::getProvincia,
                                                                Collectors.counting()
                                                            ));

    List<Estadistica> estadisticas = cantidadPorProvincia.entrySet().stream()
                                                         .map(entry -> new Estadistica(
                                                             entry.getKey(),
                                                             entry.getValue(),
                                                             categoria,
                                                             null,
                                                             "HECHOS POR PROVINCIA Y CATEGORIA"
                                                         ))
                                                         .collect(Collectors.toList());
    return estadisticas;
  }

  //Cantidad de hechos por hora segun categoria
  public List<Estadistica> hechosPorHora(String categoria) {
    List<Coleccion> colecciones = ColeccionRepository.instance().findAll();
    List<Hecho> todosLosHechos = obtenerTodosLosHechos(colecciones);

    List<Hecho> hechosFiltrados = todosLosHechos.stream()
                                                .filter(h -> categoria.equals(h.getCategoria()))
                                                .collect(Collectors.toList());

    Map<String, Long> cantidadPorHora = hechosFiltrados.stream()
                                                         .collect(Collectors.groupingBy(
                                                             h -> String.format("%02d", h.getFechasuceso().getHour()),
                                                             Collectors.counting()
                                                         ));

    List<Estadistica> estadisticas = cantidadPorHora.entrySet().stream()
                                                         .map(entry -> new Estadistica(
                                                             entry.getKey(),
                                                             entry.getValue(),
                                                             categoria,
                                                             null,
                                                             "HECHOS POR HORA Y CATEGORIA"
                                                         ))
                                                         .collect(Collectors.toList());
    return estadisticas;
  }
  //Cantidad de hechos reportados por categoria
  public List<Estadistica> hechosPorCategoria() {
    List<Coleccion> colecciones = ColeccionRepository.instance().findAll();
    List<Hecho> todosLosHechos = obtenerTodosLosHechos(colecciones);

    Map<String, Long> cantidadPorCategoria = todosLosHechos.stream()
                                                           .collect(Collectors.groupingBy(
                                                               Hecho::getCategoria,
                                                               Collectors.counting()
                                                           ));

    List<Estadistica> estadisticas = cantidadPorCategoria.entrySet().stream()
                                                    .map(entry -> new Estadistica(
                                                        null,
                                                        entry.getValue(),
                                                        entry.getKey(),
                                                        null,
                                                        "HECHOS REPORTADOS POR CATEGORIA"
                                                    ))
                                                    .collect(Collectors.toList());
    return estadisticas;
  }
  //cantidad de hechos reportados por provincia segun coleccion
  public List<Estadistica> hechosPorProvinciaDeUnaColeccion(Coleccion coleccion) {
    Filtro filtroExcluyente = obtenerFiltroExcluyente();
    List<Hecho> hechosFiltrados = coleccion.obtenerHechosFiltrados(filtroExcluyente);

    Map<String, Long> cantidadPorProvincia = hechosFiltrados.stream()
                                                            .collect(Collectors.groupingBy(
                                                                Hecho::getProvincia,
                                                                Collectors.counting()
                                                            ));

    List<Estadistica> estadisticas = cantidadPorProvincia.entrySet().stream()
                                                    .map(entry -> new Estadistica(
                                                        entry.getKey(),
                                                        entry.getValue(),
                                                        null,
                                                        coleccion,
                                                        "HECHOS REPORTADOS POR PROVINCIA Y COLECCION"
                                                    ))
                                                    .collect(Collectors.toList());
    return estadisticas;
  }
  //Cantidad de hechos
  public Estadistica calcularStatsCantHechos() {
    List<Coleccion> colecciones = ColeccionRepository.instance()
                                                     .findAll();
    List<Hecho> todosLosHechos = obtenerTodosLosHechos(colecciones);
    return new Estadistica(
        null,
        (long) todosLosHechos.size(),
        null,
        null,
        "CANTIDAD DE HECHOS"
    );
  }
  //Cantidad de solicitudes pendientes
  public Estadistica calcularStatsCantSolicitudes() {
    return new Estadistica(
        null,
        (long) gestor.getSolicitudesPendientes().size(),
        null,
        null,
        "CANTIDAD DE SOLICITUDES PENDIENTES "
    );
  }
  //Cantidad de solicitudes spam
  public Estadistica calcularStatsCantSpam() {
    return new Estadistica(
        null,
        (long) gestor.getSpam().size(),
        null,
        null,
        "CANTIDAD DE SPAM"
    );
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
                                            .flatMap(coleccion -> coleccion.obtenerHechosFiltrados(filtroExcluyente)
                                                                           .stream())
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
