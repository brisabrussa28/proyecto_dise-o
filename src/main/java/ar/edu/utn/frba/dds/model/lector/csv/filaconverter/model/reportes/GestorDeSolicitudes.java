package ar.edu.utn.frba.dds.model.lector.csv.filaconverter.model.reportes;

import ar.edu.utn.frba.dds.model.lector.csv.filaconverter.model.exceptions.SolicitudInexistenteException;
import ar.edu.utn.frba.dds.model.lector.csv.filaconverter.model.filtro.Filtro;
import ar.edu.utn.frba.dds.model.lector.csv.filaconverter.model.filtro.condiciones.CondicionPredicado;
import ar.edu.utn.frba.dds.model.lector.csv.filaconverter.model.hecho.Hecho;
import ar.edu.utn.frba.dds.model.lector.csv.filaconverter.model.reportes.detectorspam.DetectorSpam;
import ar.edu.utn.frba.dds.model.lector.csv.filaconverter.model.reportes.detectorspam.DetectorSpamTFIDF;
import ar.edu.utn.frba.dds.model.lector.csv.filaconverter.model.usuario.Usuario;
import ar.edu.utn.frba.dds.model.lector.csv.filaconverter.repositories.SolicitudesRepository;
import ar.edu.utn.frba.dds.model.lector.csv.filaconverter.repositories.TFIDFRepository;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Gestor de Solicitudes. Orquesta la lógica de negocio para crear,
 * clasificar y procesar las solicitudes de eliminación de hechos.
 *
 * Utiliza DOS detectores de spam en simultáneo:
 * 1. Detector original (inyectado por método)
 * 2. Detector TF-IDF (siempre activo, basado en aprendizaje)
 *
 * Una solicitud es marcada como spam si CUALQUIERA de los dos detectores la identifica como tal.
 */
public class GestorDeSolicitudes {

  private SolicitudesRepository repositorio;
  private TFIDFRepository tfidfRepository;
  private DetectorSpamTFIDF detectorTFIDF;

  // Detector simple por defecto para cuando no se inyecta uno
  private static class DetectorSpamBasico implements DetectorSpam {
    @Override
    public boolean esSpam(String texto) {
      if (texto == null || texto.trim().isEmpty()) {
        return false;
      }

      String textoLower = texto.toLowerCase();

      // Patrones básicos de spam
      String[] patronesSpam = {
          "compra ya", "oferta exclusiva", "descuento", "dinero rápido",
          "trabajar desde casa", "clic aquí", "urgente", "ayuda financiera",
          "producto milagroso", "gane dinero", "oportunidad única"
      };

      for (String patron : patronesSpam) {
        if (textoLower.contains(patron)) {
          return true;
        }
      }

      // Detectar exclamaciones excesivas
      long exclamaciones = texto.chars().filter(ch -> ch == '!').count();
      if (exclamaciones > 3) {
        return true;
      }

      return false;
    }
  }

  public GestorDeSolicitudes() {
    this.repositorio = SolicitudesRepository.instance();
    this.tfidfRepository = TFIDFRepository.instance();
    this.detectorTFIDF = new DetectorSpamTFIDF();
  }

  public GestorDeSolicitudes(SolicitudesRepository repositorio) {
    this.repositorio = repositorio;
    this.tfidfRepository = TFIDFRepository.instance();
    this.detectorTFIDF = new DetectorSpamTFIDF();
  }

  /**
   * Crea una nueva solicitud, la clasifica usando AMBOS detectores simultáneamente.
   * Si no se proporciona un detector externo, se usa uno básico.
   *
   * @param hecho        El hecho a solicitar eliminación.
   * @param motivo       La razón de la solicitud.
   * @param detectorSpam El detector de spam a utilizar. (Inyectado por método)
   */
  public Solicitud crearSolicitud(
      Hecho hecho,
      String motivo,
      Usuario usuarie,
      DetectorSpam detectorSpam
  ) {
    Solicitud nuevaSolicitud = new Solicitud(hecho, motivo, usuarie);

    // Usar AMBOS detectores simultáneamente
    DetectorSpam detectorBase = (detectorSpam != null) ? detectorSpam : new DetectorSpamBasico();

    boolean esSpamBase = detectorBase.esSpam(nuevaSolicitud.getRazonEliminacion());
    boolean esSpamTFIDF = detectorTFIDF.esSpam(nuevaSolicitud.getRazonEliminacion());
    boolean esSpam = esSpamBase || esSpamTFIDF;

    if (esSpam) {
      nuevaSolicitud.marcarComoSpam();

      // Si TF-IDF detectó el spam y no está ya registrado, aprender de él
      if (esSpamTFIDF && !tfidfRepository.existeTextoSimilar(motivo.trim())) {
        detectorTFIDF.entrenarConSpam(motivo);
      }
    }
    // Si no es spam, se queda como PENDIENTE por defecto.

    repositorio.guardar(nuevaSolicitud);
    return nuevaSolicitud;
  }

  /**
   * Crea una nueva solicitud usando solo los detectores internos.
   * Equivalente a llamar a crearSolicitud(hecho, motivo, null)
   */
  public Solicitud crearSolicitud(Hecho hecho, String motivo, Usuario usuario) {
    return crearSolicitud(hecho, motivo, usuario, null);
  }

  /**
   * Procesa una solicitud existente para aceptarla o rechazarla.
   *
   * @param solicitud La solicitud a gestionar.
   * @param decision  La decisión (ACEPTAR o RECHAZAR).
   */
  public void gestionarSolicitud(Solicitud solicitud, AceptarSolicitud decision) {
    // Verificamos que la solicitud exista en estado PENDIENTE
    if (!repositorio.obtenerPorEstado(EstadoSolicitud.PENDIENTE)
                    .contains(solicitud)) {
      throw new SolicitudInexistenteException("La solicitud no existe o no está pendiente.");
    }

    if (decision == AceptarSolicitud.ACEPTAR) {
      solicitud.aceptar();
    } else {
      solicitud.rechazar();
    }

    repositorio.guardar(solicitud); // Guardamos la solicitud con su nuevo estado
  }

  /**
   * Devuelve las solicitudes marcadas como spam.
   *
   * @return Lista de solicitudes de spam.
   */
  public List<Solicitud> getSpam() {
    return repositorio.obtenerPorEstado(EstadoSolicitud.SPAM);
  }

  /**
   * Devuelve las solicitudes marcadas como pendiente.
   *
   * @return Lista de solicitudes pendientes.
   */
  public List<Solicitud> getSolicitudesPendientes() {
    return repositorio.obtenerPorEstado(EstadoSolicitud.PENDIENTE);
  }

  /**
   * Devuelve una lista de todos los hechos que fueron eliminados
   * (es decir, cuyas solicitudes fueron ACEPTADAS).
   *
   * @return Lista de hechos eliminados.
   */
  public List<Hecho> obtenerHechosEliminados() {
    return repositorio.obtenerPorEstado(EstadoSolicitud.ACEPTADA)
                      .stream()
                      .map(Solicitud::getHechoSolicitado)
                      .collect(Collectors.toList());
  }

  public List<Hecho> obtenerHechosReportados() {
    return repositorio.findAll().stream()
                      .map(Solicitud::getHechoSolicitado)
                      .distinct()
                      .collect(Collectors.toList());
  }

  /**
   * Devuelve un filtro que puede ser usado para excluir los hechos eliminados
   * de otros reportes.
   *
   * @return Un Filtro que excluye los hechos eliminados.
   */
  public Filtro filtroExcluyenteDeHechosEliminados() {
    List<Hecho> hechosEliminados = this.obtenerHechosEliminados();
    return new Filtro(new CondicionPredicado(h -> !hechosEliminados.contains(h)));
  }

  /**
   * Entrena el detector TF-IDF con un nuevo ejemplo de spam
   */
  public void entrenarTFIDFConSpam(String textoSpam) {
    if (textoSpam != null && !textoSpam.trim().isEmpty()) {
      detectorTFIDF.entrenarConSpam(textoSpam);
    }
  }

  /**
   * Obtiene estadísticas de ambos detectores
   */
  public String obtenerEstadisticasDetectores() {
    StringBuilder stats = new StringBuilder();
    stats.append("=== ESTADISTICAS DETECTORES SPAM ===\n");

    // Estadísticas TF-IDF
    Long totalTFIDF = tfidfRepository.contarVectoresSpam();
    stats.append("TF-IDF: ").append(totalTFIDF).append(" vectores de spam almacenados\n");

    // Estadísticas del repositorio
    long totalSolicitudes = repositorio.cantidadTotal();
    long totalSpam = repositorio.obtenerPorEstado(EstadoSolicitud.SPAM).size();
    stats.append("Repositorio: ").append(totalSolicitudes).append(" solicitudes totales, ")
         .append(totalSpam).append(" marcadas como spam\n");

    return stats.toString();
  }

  /**
   * Obtiene el detector TF-IDF (para testing o extensión)
   */
  public DetectorSpamTFIDF getDetectorTFIDF() {
    return detectorTFIDF;
  }
}