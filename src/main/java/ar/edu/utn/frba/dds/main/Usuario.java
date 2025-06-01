package ar.edu.utn.frba.dds.main;

import ar.edu.utn.frba.dds.domain.coleccion.Coleccion;
import ar.edu.utn.frba.dds.domain.fuentes.Fuente;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.origen.Origen;
import ar.edu.utn.frba.dds.domain.reportes.GestorDeReportes;
import ar.edu.utn.frba.dds.domain.reportes.Solicitud;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

public class Usuario {
  protected String nombre;
  protected String email;

  /**
   * Constructor Usuario.
   * */
  public Usuario(String nombre, String email) {
    this.nombre = nombre;
    this.email = email;
  }
  public Usuario() {
  }

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
  public List<Hecho> importardesdeCsv(String rutaCsv, String separador, String nombreFuente) {
    return null;
  }

  /**
   * Solicitudes.
   * */
  public Solicitud obtenerSolicitud() {
    return GestorDeReportes.getInstancia().obtenerSolicitud();
  }

  public Solicitud obtenerSolicitudPorPosicion(int posicion) {
    return GestorDeReportes.getInstancia().obtenerSolicitudPorPosicion(posicion);
  }

  public void gestionarSolicitud(Solicitud solicitud, boolean aceptarSolicitud) {
    GestorDeReportes.getInstancia().gestionarSolicitud(solicitud, aceptarSolicitud);
  }

  public Solicitud solicitarEliminacion(Hecho hecho, String motivo, Fuente fuente) {
    if (hecho == null || motivo == null || motivo.isBlank()) {
      throw new IllegalArgumentException("Hecho y motivo deben estar definidos");
    }

    Solicitud solicitud = new Solicitud(this, hecho, motivo);
    // Singleton (ver patronescreacionales si no entendes que hago aca)
    GestorDeReportes.getInstancia().agregarSolicitud(solicitud);
    return solicitud;
  }

  /**
   * Crear Hecho.
   *
   * @param fuente      FuenteDinamica
   * @param categoria   String
   * @param descripcion String
   * @param direccion   String
   * @param etiquetas   List
   * @param fecha       LocalDateTime
   * @param titulo      String
   * @param ubicacion   PuntoGeografico
   */
  public Hecho crearHecho(
      String titulo,
      String descripcion,
      String categoria,
      String direccion,
      PuntoGeografico ubicacion,
      Date fecha,
      List<String> etiquetas,
      FuenteDinamica fuente
  ) {
    Hecho hecho = new Hecho(
        titulo,
        descripcion,
        categoria,
        direccion,
        ubicacion,
        fecha,
        Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()),
        Origen.PROVISTO_CONTRIBUYENTE,
        etiquetas
    );
    fuente.agregarHecho(hecho);
    return hecho;
  }

  /**
   * Geters.
   * */
  public String getNombre() {
    return nombre;
  }

  public String getEmail() {
    return email;
  }
}
