package ar.edu.utn.frba.dds.model.reportes;

import ar.edu.utn.frba.dds.model.exceptions.SolicitudInexistenteException;
import ar.edu.utn.frba.dds.model.filtro.Filtro;
import ar.edu.utn.frba.dds.model.filtro.condiciones.CondicionPredicado;
import ar.edu.utn.frba.dds.model.hecho.Hecho;
import ar.edu.utn.frba.dds.model.reportes.detectorspam.DetectorSpam;
import ar.edu.utn.frba.dds.model.reportes.detectorspam.DetectorSpamTFIDF;
import ar.edu.utn.frba.dds.model.usuario.Usuario;
import ar.edu.utn.frba.dds.repositories.HechoRepository;
import ar.edu.utn.frba.dds.repositories.SolicitudesRepository;
import ar.edu.utn.frba.dds.repositories.TFIDFRepository;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Gestor de Solicitudes. Orquesta la lógica de negocio para crear,
 * clasificar y procesar las solicitudes de eliminación de hechos.
 */
public class GestorDeSolicitudes {

  private SolicitudesRepository repositorio;
  private TFIDFRepository tfidfRepository;
  private DetectorSpamTFIDF detectorTFIDF;
  private HechoRepository hechoRepository;

  private static class DetectorSpamBasico implements DetectorSpam {
    @Override
    public boolean esSpam(String texto) {
      if (texto == null || texto.trim().isEmpty()) return false;
      String textoLower = texto.toLowerCase();
      String[] patronesSpam = {
          "compra ya", "oferta exclusiva", "descuento", "dinero rápido",
          "trabajar desde casa", "clic aquí", "urgente", "ayuda financiera",
          "producto milagroso", "gane dinero", "oportunidad única"
      };
      for (String patron : patronesSpam) {
        if (textoLower.contains(patron)) return true;
      }
      long exclamaciones = texto.chars().filter(ch -> ch == '!').count();
      return exclamaciones > 3;
    }
  }

  public GestorDeSolicitudes() {
    this.repositorio = SolicitudesRepository.instance();
    this.tfidfRepository = TFIDFRepository.instance();
    this.detectorTFIDF = new DetectorSpamTFIDF();
    this.hechoRepository = HechoRepository.instance();
  }

  public GestorDeSolicitudes(SolicitudesRepository repositorio) {
    this.repositorio = repositorio;
    this.tfidfRepository = TFIDFRepository.instance();
    this.detectorTFIDF = new DetectorSpamTFIDF();
    this.hechoRepository = HechoRepository.instance();
  }

  public Solicitud crearSolicitud(Hecho hecho, String motivo, Usuario usuarie, DetectorSpam detectorSpam) {
    Solicitud nuevaSolicitud = new Solicitud(hecho, motivo, usuarie);

    DetectorSpam detectorBase = (detectorSpam != null) ? detectorSpam : new DetectorSpamBasico();

    boolean esSpamBase = detectorBase.esSpam(nuevaSolicitud.getRazonEliminacion());
    boolean esSpamTFIDF = detectorTFIDF.esSpam(nuevaSolicitud.getRazonEliminacion());
    boolean esSpam = esSpamBase || esSpamTFIDF;

    if (esSpam) {
      nuevaSolicitud.marcarComoSpam();
      if (esSpamTFIDF && !tfidfRepository.existeTextoSimilar(motivo.trim())) {
        detectorTFIDF.entrenarConSpam(motivo);
      }
    }

    repositorio.guardar(nuevaSolicitud);
    return nuevaSolicitud;
  }

  public Solicitud crearSolicitud(Hecho hecho, String motivo, Usuario usuario) {
    return crearSolicitud(hecho, motivo, usuario, null);
  }

  public void gestionarSolicitud(Solicitud solicitud, AceptarSolicitud decision) {
    if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
      throw new RuntimeException("La solicitud ya fue procesada anteriormente.");
    }

    switch (decision) {
      case ACEPTAR:
        try {
          solicitud.aceptar();
          hechoRepository.save(solicitud.getHechoSolicitado());
        } catch (Exception e) {
          throw new RuntimeException("Error al actualizar el estado del hecho: " + e.getMessage());
        }
        break;

      case RECHAZAR:
        solicitud.rechazar();
        break;

      case SPAM:
        solicitud.marcarComoSpam();
        this.entrenarTFIDFConSpam(solicitud.getRazonEliminacion());
        break;
    }

    repositorio.guardar(solicitud);
  }

  public List<Solicitud> getSpam() {
    return repositorio.obtenerPorEstado(EstadoSolicitud.SPAM);
  }

  public List<Solicitud> getSolicitudesPendientes() {
    return repositorio.obtenerPorEstado(EstadoSolicitud.PENDIENTE);
  }

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

  public Filtro filtroExcluyenteDeHechosEliminados() {
    List<Hecho> hechosEliminados = this.obtenerHechosEliminados();
    return new Filtro(new CondicionPredicado(h -> !hechosEliminados.contains(h)));
  }

  public void entrenarTFIDFConSpam(String textoSpam) {
    if (textoSpam != null && !textoSpam.trim().isEmpty()) {
      detectorTFIDF.entrenarConSpam(textoSpam);
    }
  }

  public String obtenerEstadisticasDetectores() {
    StringBuilder stats = new StringBuilder();
    stats.append("=== ESTADISTICAS DETECTORES SPAM ===\n");
    Long totalTFIDF = tfidfRepository.contarVectoresSpam();
    stats.append("TF-IDF: ").append(totalTFIDF).append(" vectores de spam almacenados\n");
    long totalSolicitudes = repositorio.cantidadTotal();
    long totalSpam = repositorio.obtenerPorEstado(EstadoSolicitud.SPAM).size();
    stats.append("Repositorio: ").append(totalSolicitudes).append(" solicitudes totales, ")
         .append(totalSpam).append(" marcadas como spam\n");
    return stats.toString();
  }

  public DetectorSpamTFIDF getDetectorTFIDF() {
    return detectorTFIDF;
  }
}