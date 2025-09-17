package ar.edu.utn.frba.dds.domain.reportes;

import ar.edu.utn.frba.dds.domain.detectorspam.DetectorSpam;
import ar.edu.utn.frba.dds.domain.exceptions.SolicitudInexistenteException;
import ar.edu.utn.frba.dds.domain.filtro.Filtro;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Gestor de Reportes.
 */
public class RepositorioDeSolicitudes {

  private final Set<Solicitud> solicitudes = new HashSet<>();
  private final Set<Hecho> hechosEliminados = new HashSet<>(); // Usamos Set para evitar duplicados
  private final DetectorSpam detectorSpam;

  /**
   * Constructor del gestor de reportes.
   *
   * @param detectorSpam Detector de spam para filtrar solicitudes
   */
  public RepositorioDeSolicitudes(DetectorSpam detectorSpam) {
    this.detectorSpam = detectorSpam;
  }

  /**
   * Agrega una solicitud al gestor de reportes.
   * Si la razón de eliminación es spam, no se agrega.
   */
  public void agregarSolicitud(UUID id, Hecho hecho, String motivo) {

    if (hecho == null || motivo == null || motivo.isBlank()) {
      throw new IllegalArgumentException("Hecho y motivo deben estar definidos");
    }
    Solicitud solicitud = new Solicitud(id, hecho, motivo);
    if (!detectorSpam.esSpam(solicitud.getRazonEliminacion())) {
      solicitudes.add(solicitud);
    }
  }

  /**
   * Devuelve la cantidad de solicitudes registradas.
   *
   * @return Cantidad de solicitudes
   */

  public int cantidadSolicitudes() {
    return solicitudes.size();
  }

  /**
   * Obtiene la primera solicitud registrada.
   * Si no hay solicitudes, devuelve null.
   *
   * @return Primera solicitud o null si no hay solicitudes
   */
  public Solicitud obtenerSolicitud() {
    return solicitudes.isEmpty() ? null : new ArrayList<>(solicitudes).get(0);
  }

  /**
   * Gestiona una solicitud, aceptándola o rechazándola.
   * Si se acepta, marca el hecho solicitado como eliminado.
   *
   * @param solicitud        Solicitud a gestionar
   * @param aceptarSolicitud Indica si se acepta o rechaza la solicitud
   * @throws SolicitudInexistenteException Si la solicitud no existe en el gestor
   */
  public void gestionarSolicitud(Solicitud solicitud, AceptarSolicitud aceptarSolicitud) {
    if (!solicitudes.contains(solicitud)) {
      throw new SolicitudInexistenteException("La solicitud no existe en el gestor.");
    }

    solicitudes.remove(solicitud);

    if (aceptarSolicitud == AceptarSolicitud.ACEPTAR) {
      marcarComoEliminado(solicitud.getHechoSolicitado());
    }
  }

  /**
   * Marca un hecho como eliminado.
   * Si el hecho ya está eliminado, no se agrega nuevamente.
   *
   * @param hecho Hecho a marcar como eliminado
   */
  public void marcarComoEliminado(Hecho hecho) {
    if (hecho == null) {
      throw new NullPointerException("Hecho no puede ser null");
    }
    hechosEliminados.add(hecho);
  }

  /**
   * Devuelve una lista de todos los hechos eliminados.
   *
   * @return Lista de hechos eliminados
   */
  public List<Hecho> hechosEliminados() {
    return new ArrayList<>(hechosEliminados);
  }

  /**
   * Devuelve un filtro que excluye los hechos eliminados.
   * Este filtro se puede usar para filtrar hechos en reportes.
   *
   * @return Filtro que excluye los hechos eliminados
   */
  public Filtro filtroExcluyente() {
    return new Filtro() {
      @Override
      public List<Hecho> filtrar(List<Hecho> hechos) {
        return hechos.stream()
                     .filter(h -> !hechosEliminados.contains(h))
                     .toList();
      }
    };
  }

  public int cantidadDeSpamDetectado(){
    return detectorSpam.cantidadDetectada();
  }

}