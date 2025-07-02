package ar.edu.utn.frba.dds.domain.fuentes;

import ar.edu.utn.frba.dds.domain.coleccion.Coleccion;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.origen.Origen;
import ar.edu.utn.frba.dds.domain.rol.Rol;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Clase fuente dinámica.
 */
public class FuenteDinamica implements Fuente {
  List<Hecho> hechos;
  private final String nombre;

  /**
   * Constructor de la clase FuenteDinamica.
   *
   * @param nombre Nombre de la fuente dinámica.
   * @param hechos Lista de hechos iniciales para la fuente dinámica.
   */
  public FuenteDinamica(String nombre, List<Hecho> hechos) {
    this.validarFuente(nombre);
    this.nombre = nombre;
    this.hechos = hechos != null ? new ArrayList<>(hechos) : new ArrayList<>();
  }

  /**
   * Agrega un hecho a la fuente dinámica.
   *
   * @param hecho Hecho a agregar a la fuente dinámica.
   */
  public void agregarHecho(Hecho hecho) {
    this.hechos.add(hecho);
  }

  public Hecho crearHecho(
      String titulo,
      String descripcion,
      String categoria,
      String direccion,
      PuntoGeografico ubicacion,
      LocalDateTime fecha,
      List<String> etiquetas
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

    agregarHecho(hecho);
    return hecho;
  }

  public Coleccion crearColeccion(String titulo, String descripcion, String categoria) {
    return new Coleccion(titulo, this, descripcion, categoria);
  }

  /**
   * Obtiene los hechos de la fuente dinámica.
   *
   * @return Lista de hechos de la fuente dinámica.
   */
  @Override
  public List<Hecho> obtenerHechos() {
    return Collections.unmodifiableList(this.hechos);
  }

  public String getNombre() { return nombre;}

}
