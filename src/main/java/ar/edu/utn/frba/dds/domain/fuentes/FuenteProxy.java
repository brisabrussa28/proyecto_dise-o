package ar.edu.utn.frba.dds.domain.fuentes;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.List;

/**
 * Clase fuente proxy.
 */
public class FuenteProxy extends Fuente {

  /**
   * Constructor.
   */
  public FuenteProxy(String nombre) {
    super(nombre);
  }

  @Override
  public List<Hecho> obtenerHechos() {
    // Simulación de integración con fuente externa
    return List.of(); // por ahora vacío
  }
}