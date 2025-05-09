package ar.edu.utn.frba.dds.domain.reportes;

import ar.edu.utn.frba.dds.domain.exceptions.SolicitudInexistenteException;
import ar.edu.utn.frba.dds.domain.fuentes.Fuente;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Gestor de Reportes.
 */
public class GestorDeReportes {
  private GestorDeReportes instancia; // Singleton instance
  private List<Solicitud> solicitudes;

  private GestorDeReportes() {
    this.solicitudes = new ArrayList<>();
  }

  /**
   * Gestor de Reportes es clase singleton.
   */
  public GestorDeReportes getInstancia() {
    if (instancia == null) {
      instancia = new GestorDeReportes();
    }
    return instancia;
  }

  /**
   * Agrega una solicitud a la lista.
   */
  public void agregarSolicitud(Solicitud solicitud) {
    this.solicitudes.add(solicitud);
  }

  /**
   * Solicitud.
   *
   * @param posicion int.
   */
  public Solicitud obtenerSolicitudPorPosicion(int posicion) {
    if (posicion < 0 || posicion >= solicitudes.size()) {
      throw new SolicitudInexistenteException("La posición es inválida o no existe en el gestor.");
    }
    return solicitudes.get(posicion);
  }

  /**
   * Función creada para el test, devuelve la cantidad de solicitudes.
   */
  public int cantidadSolicitudes() {
    return solicitudes.size();
  }

  /**
   * Obtiene la primera solicitud de la lista.
   */
  public Solicitud obtenerSolicitud() {
    return this.obtenerSolicitudPorPosicion(0);
  }

  /**
   * Gestionar Solicitud.
   *
   * @param solicitud        Solicitud
   * @param aceptarSolicitud boolean
   */
  public void gestionarSolicitud(Solicitud solicitud, boolean aceptarSolicitud) {
    if (!solicitudes.contains(solicitud)) {
      throw new SolicitudInexistenteException("La solicitud no existe en el gestor.");
    }

    solicitudes.remove(solicitud);

    if (aceptarSolicitud) {
      eliminarHecho(solicitud.getHechoSolicitado(), solicitud.getFuente());
    }
  }

  private void eliminarHecho(UUID idHecho, Fuente fuente) {
    fuente.eliminarHecho(idHecho);
  }

}