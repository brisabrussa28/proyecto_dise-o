package ar.edu.utn.frba.dds.domain.filtro;


import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.List;

/**
 * Ejecuta una operación sobre una lista de hechos.
 */
public interface Operacion {
  /**
   * Ejecuta una operación sobre una lista de hechos.
   *
   * @param hechos Lista de hechos sobre los que se ejecutará la operación.
   * @return Lista de hechos resultantes de la operación.
   */
  List<Hecho> ejecutar(List<Hecho> hechos);
}