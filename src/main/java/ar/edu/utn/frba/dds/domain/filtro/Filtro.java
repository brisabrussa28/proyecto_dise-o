package ar.edu.utn.frba.dds.domain.filtro;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Clase base para filtros de hechos.
 */
public class Filtro {

  /**
   * Aplica el filtro a una lista de hechos.
   */
  public List<Hecho> filtrar(List<Hecho> hechos) {
    if (hechos == null || hechos.isEmpty()) {
      return Collections.emptyList();
    }
    return hechos.stream().filter(this::cumple).collect(Collectors.toList());
  }

  /**
   * Condición específica del filtro.
   */
  public boolean cumple(Hecho hecho){
    return true;
  }

  /**
   * Devuelve el predicado del filtro.
   */
  public Predicate<Hecho> comoPredicado() {
    return this::cumple;
  }
}