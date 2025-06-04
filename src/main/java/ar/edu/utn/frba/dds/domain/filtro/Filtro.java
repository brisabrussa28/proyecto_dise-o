package ar.edu.utn.frba.dds.domain.filtro;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Clase base para filtros de hechos.
 */
public class Filtro {
  private final Operacion operacion;

  public Filtro(Operacion operacion) {
    this.operacion = operacion;
  }

  public List<Hecho> filtrar(List<Hecho> hechos) {
    return operacion.ejecutar(hechos);
  }

}
