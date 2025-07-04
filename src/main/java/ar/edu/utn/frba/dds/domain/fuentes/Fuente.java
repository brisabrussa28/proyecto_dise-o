package ar.edu.utn.frba.dds.domain.fuentes;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.List;

/**
 * Clase fuente.
 */
public interface Fuente {

  /**
   * Obtiene los hechos de la fuente.
   *
   * @return Lista de hechos de la fuente.
   */
  List<Hecho> obtenerHechos();

  default void validarFuente(String nombre) {
    if (nombre == null || nombre.isEmpty()) {
      throw new RuntimeException("El nombre de la fuente no puede ser nulo ni vacío.");
    }
  }
}