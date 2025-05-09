package ar.edu.utn.frba.dds.domain.fuentes;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.Collections;
import java.util.List;

/**
 * Clase fuente dinámica.
 */
public class FuenteDinamica extends Fuente {

  /**
   * Constructor.
   */
  public FuenteDinamica(String nombre, List<Hecho> hechos) {
    super(nombre, hechos);
  }

  /**
   * Agregar hecho a la fuente.
   */
  public void agregarHecho(Hecho hecho) {
    this.hechos.add(hecho);
  }

  /**
   * Obtiene los hechos de la fuente (vista inmutable).
   */
  @Override
  public List<Hecho> obtenerHechos() {
    return Collections.unmodifiableList(this.hechos);
  }
}
