package ar.edu.utn.frba.dds.domain.filtro;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase filtro lista or.
 */
public class FiltroListaOr extends Filtro {
  /**
   * Constructor de FiltroListaOr.
   *
   * @param filtros Lista de filtros a combinar con una operación OR.
   */

  public FiltroListaOr(List<Filtro> filtros) {
    super(hechos -> filtros.stream()
        .flatMap(f -> f.filtrar(hechos).stream())
        .distinct()
        .toList());
  }
}
