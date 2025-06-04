package ar.edu.utn.frba.dds.domain.reportes;

import ar.edu.utn.frba.dds.domain.detectorspam.DetectorSpam;
import ar.edu.utn.frba.dds.domain.exceptions.SolicitudInexistenteException;
import ar.edu.utn.frba.dds.domain.filtro.Filtro;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Gestor de Reportes.
 */
public class GestorDeReportes {

  private final DetectorSpam detectorSpam;
  private final List<Solicitud> solicitudes = new ArrayList<>();
  private final Set<Hecho> hechosEliminados = new HashSet<>(); // Usamos Set para evitar duplicados

  /**
   * Constructor del gestor de reportes.
   *
   * @param detectorSpam Detector de spam para filtrar solicitudes
   */
  public GestorDeReportes(DetectorSpam detectorSpam) {
    this.detectorSpam = detectorSpam;
  }

  /**
   * Agrega una solicitud al gestor de reportes.
   * Si la razón de eliminación es spam, no se agrega.
   *
   * @param solicitud Solicitud a agregar
   */
  public void agregarSolicitud(Solicitud solicitud) {
    if (!detectorSpam.esSpam(solicitud.getRazonEliminacion())) {
      solicitudes.add(solicitud);
    }
  }

  /**
   * Devuelve una lista de todas las solicitudes registradas.
   *
   * @param posicion Posición de la solicitud a obtener
   * @return Lista de solicitudes
   */
  public Solicitud obtenerSolicitudPorPosicion(int posicion) {
    if (posicion < 0 || posicion >= solicitudes.size()) {
      throw new SolicitudInexistenteException("La posición es inválida o no existe en el gestor.");
    }
    return solicitudes.get(posicion);
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
    return solicitudes.isEmpty() ? null : solicitudes.get(0);
  }

  /**
   * Gestiona una solicitud, aceptándola o rechazándola.
   * Si se acepta, marca el hecho solicitado como eliminado.
   *
   * @param solicitud        Solicitud a gestionar
   * @param aceptarSolicitud Indica si se acepta o rechaza la solicitud
   * @throws SolicitudInexistenteException Si la solicitud no existe en el gestor
   */
  public void gestionarSolicitud(Solicitud solicitud, boolean aceptarSolicitud) {
    if (!solicitudes.contains(solicitud)) {
      throw new SolicitudInexistenteException("La solicitud no existe en el gestor.");
    }

    solicitudes.remove(solicitud);

    if (aceptarSolicitud) {
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
    return new Filtro(hechos ->
                          hechos.stream()
                                .filter(h -> !hechosEliminados.contains(h))
                                .toList()
    );
  }
}