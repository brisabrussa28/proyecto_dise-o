package ar.edu.utn.frba.dds.domain.fuentes;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.List;

/**
 * Clase fuente proxy.
 */
public class FuenteProxy extends Fuente {

  /**
   * Constructor de la clase FuenteProxy.
   * Este constructor inicializa el nombre de la fuente.
   *
   * @param nombre Nombre de la fuente proxy.
   */
  public FuenteProxy(String nombre) {
    super(nombre);
  }

  /**
   * Obtiene los hechos de la fuente proxy.
   *
   * @return Lista de hechos obtenidos de la fuente proxy.
   */
  @Override
  public List<Hecho> obtenerHechos() {
    // Simulación de integración con fuente externa
    return List.of(); // por ahora vacío
  }
}