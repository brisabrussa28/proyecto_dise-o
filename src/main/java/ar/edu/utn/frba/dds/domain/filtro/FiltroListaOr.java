package ar.edu.utn.frba.dds.domain.filtro;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase filtro lista or.
 */
public class FiltroListaOr extends Filtro {
  List<Filtro> filtros;

  /**
   * Constructor.
   */
  public FiltroListaOr(List<Filtro> filtros) {
    this.filtros = new ArrayList<>(filtros);
  }

  @Override
  public List<Hecho> filtrar(List<Hecho> hechos) {
    return filtros.stream()
        .flatMap(filtro -> filtro.filtrar(hechos).stream())
        .distinct()  // eliminar duplicados si corresponde
        .toList();
  }
}
