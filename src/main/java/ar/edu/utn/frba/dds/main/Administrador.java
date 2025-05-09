package ar.edu.utn.frba.dds.main;

import ar.edu.utn.frba.dds.domain.coleccion.Coleccion;
import ar.edu.utn.frba.dds.domain.csv.LectorCsv;
import ar.edu.utn.frba.dds.domain.fuentes.Fuente;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteEstatica;
import ar.edu.utn.frba.dds.domain.reportes.GestorDeReportes;
import ar.edu.utn.frba.dds.domain.reportes.Solicitud;

/**
 * Administrador.
 * */
public class Administrador extends Persona {
  /**
   * Constructor del administrador.
   * */
  public Administrador(String nombre, String email) {
    super(nombre, email);
  }

  /**
   * Constructor del administrador.
   * */
  public Coleccion crearColeccion(
      String titulo,
      String descripcion,
      String categoria,
      Fuente fuente
  ) {
    return new Coleccion(titulo, fuente, descripcion, categoria);
  }

  /**
   * Fuente Estatica.
   * */
  public FuenteEstatica importardesdeCsv(String rutaCsv, String separador, String nombreFuente) {
    LectorCsv lector = new LectorCsv();
    return lector.importar(rutaCsv, separador, nombreFuente);
  }

  /**
   * Fuente Estatica.
   * */
  public Solicitud obtenerSolicitud() {
    return GestorDeReportes.getInstancia().obtenerSolicitud();
  }

  /**
   * Fuente Estatica.
   * */
  public Solicitud obtenerSolicitudPorPosicion(int posicion) {
    return GestorDeReportes.getInstancia().obtenerSolicitudPorPosicion(posicion);
  }

  /**
   * Fuente Estatica.
   * */
  public void gestionarSolicitud(Solicitud solicitud, boolean aceptarSolicitud) {
    GestorDeReportes.getInstancia().gestionarSolicitud(solicitud, aceptarSolicitud);
  }

}

