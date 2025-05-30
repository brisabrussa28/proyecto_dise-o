package ar.edu.utn.frba.dds.domain.filtro;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase filtro lista or.
 */
public class FiltroListaOr extends Filtro {
  private final List<Filtro> filtros;

  public FiltroListaOr(List<Filtro> filtros) {
    this.filtros = filtros;
  }

  @Override
  public boolean cumple(Hecho hecho) {
    return filtros.stream().anyMatch(f -> f.cumple(hecho));
  }
}
