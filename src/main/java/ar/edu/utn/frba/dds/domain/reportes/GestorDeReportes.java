package ar.edu.utn.frba.dds.domain.reportes;

import ar.edu.utn.frba.dds.domain.exceptions.SolicitudInexistenteException;
import ar.edu.utn.frba.dds.domain.filtro.Filtro;
import ar.edu.utn.frba.dds.domain.filtro.FiltroIgualHecho;
import ar.edu.utn.frba.dds.domain.filtro.FiltroNot;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;

import java.util.ArrayList;
import java.util.List;

/**
 * Gestor de Reportes.
 */
public class GestorDeReportes {

  private static GestorDeReportes instancia; // Singleton
  private static List<Solicitud> solicitudes = new ArrayList<>();
  private List<Filtro> HechosEliminados = new ArrayList<>();

  private GestorDeReportes() {
  }

  /**
   * Devuelve la instancia única del gestor.
   */
  public static GestorDeReportes getInstancia() {
    if (instancia == null) {
      instancia = new GestorDeReportes();
    }
    return instancia;
  }

  /**
   * Agrega una solicitud a la lista.
   */
  public void agregarSolicitud(Solicitud solicitud) {
    solicitudes.add(solicitud);
  }

  /**
   * Devuelve una solicitud por posición.
   *
   * @param posicion índice de la lista
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
   *
   * @param solicitud        la solicitud a gestionar
   * @param aceptarSolicitud si se aprueba o no
   */
  public static void gestionarSolicitud(Solicitud solicitud, boolean aceptarSolicitud) {
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
  public static void eliminarHecho(Hecho hecho) {
    Filtro filtroDeExclusion = new FiltroNot(new FiltroIgualHecho(hecho));
    getInstancia().HechosEliminados.add(filtroDeExclusion);
  }

  /**
   * Devuelve una copia de los filtros de hechos eliminados.
   */
  public static List<Filtro> hechosEliminados() {
    return new ArrayList<>(getInstancia().HechosEliminados);
  }
}
