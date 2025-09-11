package ar.edu.utn.frba.dds.domain.filtro;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.Collections;
import java.util.List;

/**
 * Clase filtro lista or.
 */
public class FiltroListaOr implements Filtro {
  private final List<Filtro> filtros;

  public FiltroListaOr(List<Filtro> filtros) {
    this.filtros = Collections.unmodifiableList(filtros);
  }

  @Override
  public List<Hecho> filtrar(List<Hecho> hechos) {
    return filtros.stream()
                  .flatMap(f -> f.filtrar(hechos).stream())
                  .distinct()
                  .toList();
  }

}
