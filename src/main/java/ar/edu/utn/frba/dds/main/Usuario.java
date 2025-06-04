package ar.edu.utn.frba.dds.main;

import ar.edu.utn.frba.dds.domain.coleccion.Coleccion;
import ar.edu.utn.frba.dds.domain.fuentes.Fuente;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteEstatica;
import ar.edu.utn.frba.dds.domain.hecho.CampoHecho;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.origen.Origen;
import ar.edu.utn.frba.dds.domain.reportes.GestorDeReportes;
import ar.edu.utn.frba.dds.domain.reportes.Solicitud;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Usuario {
  protected String nombre;
  protected String email;
  protected UUID id;

  /**
   * Constructor Usuario.
   * */
  public Usuario(String nombre, String email) {
    this.nombre = nombre;
    this.email = email;
    this.id = UUID.randomUUID();
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
  public FuenteEstatica importardesdeCsv(String rutaCsv, char separador, String nombreFuente,String formatoFecha, Map< CampoHecho, List<String>> mapeo) {
    if (rutaCsv == null ||  nombreFuente == null) {
      throw new IllegalArgumentException("Ruta y nombre de fuente deben estar definidos");
    }

    return new FuenteEstatica(nombreFuente,rutaCsv, separador,formatoFecha,mapeo );
  }
  /**
   * * Fuente Estatica con separador coma.
   * */
  public FuenteEstatica importardesdeCsv(String rutaCsv, String nombreFuente,String formatoFecha, Map< CampoHecho, List<String>> mapeo) {
    if (rutaCsv == null || nombreFuente == null) {
      throw new IllegalArgumentException("Ruta y nombre de fuente deben estar definidos");
    }
    return new FuenteEstatica(nombreFuente,rutaCsv,',',formatoFecha,mapeo );
  }

  /**
   * Solicitudes.
   * */
  public Solicitud obtenerSolicitud(GestorDeReportes gestorDeReportes) {
    return gestorDeReportes.obtenerSolicitud();
  }

  public Solicitud obtenerSolicitudPorPosicion(int posicion,GestorDeReportes gestorDeReportes) {
    return gestorDeReportes.obtenerSolicitudPorPosicion(posicion);
  }

  public void gestionarSolicitud(Solicitud solicitud, boolean aceptarSolicitud,GestorDeReportes gestorDeReportes) {
    gestorDeReportes.gestionarSolicitud(solicitud, aceptarSolicitud);
  }

  public Solicitud solicitarEliminacion(Hecho hecho, String motivo, Fuente fuente, GestorDeReportes gestorDeReportes) {
    if (hecho == null || motivo == null || motivo.isBlank()) {
      throw new IllegalArgumentException("Hecho y motivo deben estar definidos");
    }

    Solicitud solicitud = new Solicitud(this, hecho, motivo);
    // Singleton (ver patronescreacionales si no entendes que hago aca)
    gestorDeReportes.agregarSolicitud(solicitud);
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
