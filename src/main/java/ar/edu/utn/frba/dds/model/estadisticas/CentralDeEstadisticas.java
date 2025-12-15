package ar.edu.utn.frba.dds.model.estadisticas;

import ar.edu.utn.frba.dds.model.coleccion.Coleccion;
import ar.edu.utn.frba.dds.model.exportador.Exportador;
import ar.edu.utn.frba.dds.model.filtro.Filtro;
import ar.edu.utn.frba.dds.model.hecho.Hecho;
import ar.edu.utn.frba.dds.model.reportes.GestorDeSolicitudes;
import ar.edu.utn.frba.dds.model.reportes.Solicitud;
import ar.edu.utn.frba.dds.repositories.ColeccionRepository;
import ar.edu.utn.frba.dds.repositories.HechoRepository;
import ar.edu.utn.frba.dds.utils.DBUtils;

import java.util.Set;
import javax.persistence.EntityManager;
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

  // Cantidad de hechos por provincia segun categoria
  public List<Estadistica> hechosPorProvinciaSegunCategoria(String categoria) {
    List<Hecho> hechosFiltrados = obtenerHechosPorCategoria(categoria);

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

  // Cantidad de hechos por hora segun categoria
  public List<Estadistica> hechosPorHora(String categoria) {
    List<Hecho> hechosFiltrados = obtenerHechosPorCategoria(categoria);

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

  // Cantidad de hechos reportados por categoria
  public List<Estadistica> hechosPorCategoria() {
    List<Hecho> hechosReportados = gestor.obtenerHechosReportados();

    Map<String, Long> cantidadPorCategoria = hechosReportados.stream()
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

  // cantidad de hechos reportados por provincia segun coleccion
  public List<Estadistica> hechosPorProvinciaDeUnaColeccion(Coleccion coleccion) {
    // Asegurar que la colección tenga los hechos cargados
    Coleccion coleccionCompleta = cargarColeccionCompleta(coleccion.getId());

    List<Hecho> hechosReportados = gestor.obtenerHechosReportados();
    Set<Hecho> hechosDeColeccion = coleccionCompleta.getHechos();

    // Filtrar solo los hechos que están en ambos conjuntos
    List<Long> idsHechosReportados = hechosReportados.stream()
                                                     .map(Hecho::getId)
                                                     .collect(Collectors.toList());

    List<Hecho> interseccion = hechosDeColeccion.stream()
                                                .filter(h -> idsHechosReportados.contains(h.getId()))
                                                .collect(Collectors.toList());

    Map<String, Long> cantidadPorProvincia = interseccion.stream()
                                                         .collect(Collectors.groupingBy(
                                                             Hecho::getProvincia,
                                                             Collectors.counting()
                                                         ));

    List<Estadistica> estadisticas = cantidadPorProvincia.entrySet().stream()
                                                         .map(entry -> new Estadistica(
                                                             entry.getKey(),
                                                             entry.getValue(),
                                                             null,
                                                             coleccionCompleta,
                                                             "HECHOS REPORTADOS POR PROVINCIA Y COLECCION"
                                                         ))
                                                         .collect(Collectors.toList());
    return estadisticas;
  }

  // Cantidad de hechos
  public Estadistica calcularStatsCantHechos() {
    // Usar el método countAll() del repositorio que ya existe
    long cantidadTotal = HechoRepository.instance().countAll();

    return new Estadistica(
        null,
        cantidadTotal,
        null,
        null,
        "CANTIDAD DE HECHOS"
    );
  }

  // Cantidad de solicitudes pendientes
  public Estadistica calcularStatsCantSolicitudes() {
    return new Estadistica(
        null,
        (long) gestor.getSolicitudesPendientes().size(),
        null,
        null,
        "CANTIDAD DE SOLICITUDES PENDIENTES"
    );
  }

  // Cantidad de solicitudes spam
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

  // --- MÉTODOS PRIVADOS AUXILIARES MODIFICADOS ---

  private List<Hecho> obtenerHechosPorCategoria(String categoria) {
    EntityManager em = DBUtils.getEntityManager();
    try {
      List<Hecho> hechos = em.createQuery(
                                 "SELECT DISTINCT h FROM Hecho h " +
                                     "LEFT JOIN FETCH h.etiquetas " +
                                     "LEFT JOIN FETCH h.fotos " +
                                     "WHERE h.hecho_categoria = :categoria", Hecho.class)
                             .setParameter("categoria", categoria)
                             .getResultList();

      // Aplicar filtro excluyente si es necesario
      if (gestor != null) {
        Filtro filtroExcluyente = gestor.filtroExcluyenteDeHechosEliminados();
        if (filtroExcluyente != null) {
          hechos = filtroExcluyente.filtrar(hechos);
        }
      }

      return hechos;
    } finally {
      em.close();
    }
  }

  private Coleccion cargarColeccionCompleta(Long coleccionId) {
    EntityManager em = DBUtils.getEntityManager();
    try {
      return em.createQuery(
                   "SELECT DISTINCT c FROM Coleccion c " +
                       "LEFT JOIN FETCH c.coleccion_fuente f " +
                       "LEFT JOIN FETCH f.hechosPersistidos h " +
                       "LEFT JOIN FETCH h.etiquetas " +
                       "LEFT JOIN FETCH h.fotos " +
                       "WHERE c.coleccion_id = :id", Coleccion.class)
               .setParameter("id", coleccionId)
               .getSingleResult();
    } catch (Exception e) {
      throw new RuntimeException("Error al cargar colección completa: " + e.getMessage(), e);
    } finally {
      em.close();
    }
  }

  // Método original modificado para evitar lazy loading
  private List<Hecho> obtenerTodosLosHechos(List<Coleccion> colecciones) {
    // Usar el repositorio que ya maneja correctamente las sesiones
    return HechoRepository.instance().findAll();
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