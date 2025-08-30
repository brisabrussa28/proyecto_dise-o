package ar.edu.utn.frba.dds.domain.filtro;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.List;
import java.util.function.Predicate;

public class FiltroPredicado implements Filtro {
  private final Predicate<Hecho> condicion; // Recibe un "bloque de código"

  public FiltroPredicado(Predicate<Hecho> condicion) {
    this.condicion = condicion;
  }

  @Override
  public List<Hecho> filtrar(List<Hecho> hechos) {
    return hechos.stream()
        .filter(this.condicion) // Aplica la condición recibida
        .toList();
  }
}