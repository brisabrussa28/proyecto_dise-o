package ar.edu.utn.frba.dds.domain.reportes;

import ar.edu.utn.frba.dds.domain.exceptions.SolicitudInexistenteException;
import ar.edu.utn.frba.dds.domain.filtro.Filtro;
import ar.edu.utn.frba.dds.domain.filtro.FiltroIgualHecho;
import ar.edu.utn.frba.dds.domain.filtro.FiltroNot;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.detectorSpam.DetectorSpam;

import java.util.ArrayList;
import java.util.List;

/**
 * Gestor de Reportes.
 */
public class GestorDeReportes {

  private final DetectorSpam detectorSpam;
  private final List<Solicitud> solicitudes = new ArrayList<>();
  private final List<Filtro> hechosEliminados = new ArrayList<>();

  public GestorDeReportes(DetectorSpam detectorSpam) {
    this.detectorSpam = detectorSpam;
  }

  /**
   * Agrega una solicitud a la lista si no es spam.
   */
  public void agregarSolicitud(Solicitud solicitud) {
    if (!detectorSpam.esSpam(solicitud.getRazonEliminacion())) {
      solicitudes.add(solicitud);
    }
  }

  /**
   * Devuelve una solicitud por posición.
   */
  public Solicitud obtenerSolicitudPorPosicion(int posicion) {
    if (posicion < 0 || posicion >= solicitudes.size()) {
      throw new SolicitudInexistenteException("La posición es inválida o no existe en el gestor.");
    }
    return solicitudes.get(posicion);
  }

  /**
   * Devuelve la cantidad de solicitudes registradas.
   */
  public int cantidadSolicitudes() {
    return solicitudes.size();
  }

  /**
   * Devuelve la primera solicitud de la lista.
   */
  public Solicitud obtenerSolicitud() {
    return this.obtenerSolicitudPorPosicion(0);
  }

  /**
   * Procesa una solicitud: si es aceptada, elimina el hecho.
   */
  public void gestionarSolicitud(Solicitud solicitud, boolean aceptarSolicitud) {
    if (!solicitudes.contains(solicitud)) {
      throw new SolicitudInexistenteException("La solicitud no existe en el gestor.");
    }

    solicitudes.remove(solicitud);

    if (aceptarSolicitud) {
      eliminarHecho(solicitud.getHechoSolicitado());
    }
  }

  /**
   * Agrega un filtro para excluir un hecho.
   */
  public void eliminarHecho(Hecho hecho) {
    Filtro filtroDeExclusion = new FiltroNot(new FiltroIgualHecho(hecho));
    hechosEliminados.add(filtroDeExclusion);
  }

  /**
   * Devuelve una copia de los filtros de hechos eliminados.
   */
  public List<Filtro> hechosEliminados() {
    return new ArrayList<>(hechosEliminados);
  }
}
