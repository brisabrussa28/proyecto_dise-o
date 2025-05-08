package ar.edu.utn.frba.dds.main;

import ar.edu.utn.frba.dds.domain.CSV.LectorCSV;
import ar.edu.utn.frba.dds.domain.Coleccion.Coleccion;
import ar.edu.utn.frba.dds.domain.fuentes.Fuente;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteEstatica;
import ar.edu.utn.frba.dds.domain.reportes.GestorDeReportes;
import ar.edu.utn.frba.dds.domain.reportes.Solicitud;


public class Administrador extends Persona {
  public Administrador(String nombre, String email) {
    super(nombre, email);
  }

  public Coleccion crearColeccion(String titulo, String descripcion, String categoria, Fuente fuente /*,criterio*/) {
    return new Coleccion(titulo, fuente, descripcion, categoria); //NOTE: Agregar fuente
  }
  //a este de coleccion le falta la fuente y el criterio.

  public FuenteEstatica importarDesdeCSV(String rutaCSV, String separador, String nombreFuente) {
    LectorCSV lector = new LectorCSV();
    return lector.importar(rutaCSV, separador, nombreFuente);
  }

  public Solicitud obtenerSolicitud() {
    return GestorDeReportes.getInstancia().obtenerSolicitud();
  }

  public Solicitud obtenerSolicitudPorPosicion(int posicion) {
    return GestorDeReportes.getInstancia().obtenerSolicitudPorPosicion(posicion);
  }

  public void gestionarSolicitud(Solicitud solicitud, boolean aceptarSolicitud) {
    GestorDeReportes.getInstancia().gestionarSolicitud(solicitud, aceptarSolicitud);
  }

}

