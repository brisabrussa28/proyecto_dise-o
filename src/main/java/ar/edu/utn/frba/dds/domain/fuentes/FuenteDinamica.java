package ar.edu.utn.frba.dds.domain.fuentes;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Clase fuente dinámica.
 */
public class FuenteDinamica extends Fuente {
  List<Hecho> hechos;

  /**
   * Constructor de la clase FuenteDinamica.
   *
   * @param nombre Nombre de la fuente dinámica.
   * @param hechos Lista de hechos iniciales para la fuente dinámica.
   */
  public FuenteDinamica(String nombre, List<Hecho> hechos) {
    super(nombre);
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

  /**
   * Obtiene los hechos de la fuente dinámica.
   *
   * @return Lista de hechos de la fuente dinámica.
   */
  @Override
  public List<Hecho> obtenerHechos() {
    return Collections.unmodifiableList(this.hechos);
  }
}
