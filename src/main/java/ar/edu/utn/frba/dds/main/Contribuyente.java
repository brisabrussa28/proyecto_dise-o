package ar.edu.utn.frba.dds.main;

import ar.edu.utn.frba.dds.domain.fuentes.Fuente;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.origen.Origen;
import ar.edu.utn.frba.dds.domain.reportes.GestorDeReportes;
import ar.edu.utn.frba.dds.domain.reportes.Solicitud;
import java.time.LocalDateTime;
import java.util.List;


//NOTE: Contribuyente deberia extender de Visualizador
// ya que tiene el mismo comportamiento que el visualizador además de poder cargar archivos, y
// necesitamos almacenar sí o sí el nombre sin importar que esté registrado o no y tmb otros
// datos que están en el enunciado que no son obligatorios.

/**
 * Contribuyente.
 */
public class Contribuyente extends Visualizador {

  /**
   * Constructor Contribuyente.
   */
  public Contribuyente(String nombre, String email) {
    super(nombre, email);
  }

  /**
   * Es Anonimo.
   *
   * @return boolean
   */
  public boolean esAnonimo() {
    return (this.getNombre() == null
        || this.getNombre().isBlank())
        && (this.getEmail() == null
        || this.getEmail().isBlank());
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
      LocalDateTime fecha,
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
        LocalDateTime.now(),
        Origen.PROVISTO_CONTRIBUYENTE,
        etiquetas
    );
    fuente.agregarHecho(hecho);
    return hecho;
  }

  /**
   * Solicitud.
   */
  public Solicitud solicitarEliminacion(Hecho hecho, String motivo, Fuente fuente) {
    if (hecho == null || motivo == null || motivo.isBlank()) {
      throw new IllegalArgumentException("Hecho y motivo deben estar definidos");
    }

    Solicitud solicitud = new Solicitud(this, hecho.getId(), fuente, motivo);
    // Singleton (ver patronescreacionales si no entendes que hago aca)
    GestorDeReportes.getInstancia().agregarSolicitud(solicitud);
    return solicitud;
  }

}