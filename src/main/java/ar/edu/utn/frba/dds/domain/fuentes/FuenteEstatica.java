package ar.edu.utn.frba.dds.domain.fuentes;

//Para esta iteración, se requiere diseñar e implementar el componente que posibilite la lectura de
// estos datasets y que extraiga los hechos de los mismos.
// En esta primera iteración estaremos incorporando un lote de datos estático de tipo archivo .csv.

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.Collections;
import java.util.List;

/**
 * Clase fuente estática.
 */
public class FuenteEstatica extends Fuente {

  /**
   * Constructor.
   */
  public FuenteEstatica(String nombre, List<Hecho> hechos) {
    super(nombre, hechos);
  }

  @Override
  public List<Hecho> obtenerHechos() {
    return Collections.unmodifiableList(this.hechos);
  }
}
