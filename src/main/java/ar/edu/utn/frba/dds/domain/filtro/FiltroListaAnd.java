package ar.edu.utn.frba.dds.domain.filtro;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase filtro de lista and.
 */
public class FiltroListaAnd extends Filtro {
  private final List<Filtro> filtros;

  public FiltroListaAnd(List<Filtro> filtros) {
    this.filtros = filtros;
  }

  @Override
  public boolean cumple(Hecho hecho) {
    return filtros.stream().allMatch(f -> f.cumple(hecho));
  }
}

