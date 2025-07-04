package ar.edu.utn.frba.dds.domain.fuentes;

import ar.edu.utn.frba.dds.domain.algoritmosconsenso.AlgoritmoDeConsenso;
import ar.edu.utn.frba.dds.domain.coleccion.Coleccion;
import ar.edu.utn.frba.dds.domain.hecho.Estado;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.origen.Origen;
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

  public Coleccion crearColeccion(
      String titulo,
      String descripcion,
      String categoria
  ) {
    return new Coleccion(titulo, this, descripcion, categoria);
  }

  public Coleccion crearColeccionConAlgoritmo(
      String titulo,
      String descripcion,
      String categoria,
      AlgoritmoDeConsenso algoritmo
  ) {
    return new Coleccion(titulo, this, descripcion, categoria, algoritmo);
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

  public String getNombre() {
    return nombre;
  }

  /**
   * Edita los detalles del hecho.
   * Permite cambiar el título, descripción, categoría, dirección, ubicación,
   * etiquetas y fecha de suceso del hecho si el usuario tiene permisos para editarlo.
   *
   * @param hecho Hecho
   */
  public void editarHecho(
      Hecho hecho,
      String titulo,
      String descripcion,
      String categoria,
      String direccion,
      PuntoGeografico ubicacion,
      List<String> etiquetas,
      LocalDateTime fechaSuceso
  ) {
    if (LocalDateTime.now().isAfter(hecho.getFechaCarga().plusWeeks(1))) {
      throw new RuntimeException("Flaco, te pasaste una semana");
    } else {
      if (titulo != null) {
        hecho.setTitulo(titulo);
      }
      if (descripcion != null) {
        hecho.setDescripcion(descripcion);
      }
      if (categoria != null) {
        hecho.setCategoria(categoria);
      }
      if (direccion != null) {
        hecho.setDireccion(direccion);
      }
      if (ubicacion != null) {
        hecho.setUbicacion(ubicacion);
      }
      if (etiquetas != null) {
        hecho.setEtiquetas(etiquetas);
      }
      if (fechaSuceso != null) {
        hecho.setFechaSuceso(fechaSuceso);
      }
      hecho.setEstado(Estado.EDITADO);
    }
  }

}
