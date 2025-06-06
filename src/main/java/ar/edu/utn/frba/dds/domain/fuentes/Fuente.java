package ar.edu.utn.frba.dds.domain.fuentes;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.List;

/**
 * Clase fuente.
 */
public abstract class Fuente {
  protected String nombre;

  /**
   * Constructor de la clase Fuente.
   *
   * @param nombre Nombre de la fuente.
   */
  public Fuente(String nombre) {
    this.validarFuente(nombre);
    this.nombre = nombre;
  }

  /**
   * Obtiene los hechos de la fuente.
   *
   * @return Lista de hechos de la fuente.
   */
  public abstract List<Hecho> obtenerHechos();

  /**
   * Obtiene el identificador único de la fuente.
   *
   * @return Identificador único de la fuente.
   */
  public String getNombre() {
    return nombre;
  }

  /**
   * Verifica si la fuente contiene un hecho específico.
   *
   * @param unHecho Hecho a buscar en la fuente.
   * @return true si la fuente contiene el hecho, false en caso contrario.
   */
  public boolean contiene(Hecho unHecho) {
    return obtenerHechos().contains(unHecho);
  }

  private void validarFuente(String nombre) {
    if (nombre == null || nombre.isEmpty()) {
      throw new RuntimeException("El nombre de la fuente no puede ser nulo ni vacío.");
    }
  }
}