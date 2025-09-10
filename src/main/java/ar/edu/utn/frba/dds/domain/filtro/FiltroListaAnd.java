package ar.edu.utn.frba.dds.domain.filtro;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.Collections;
import java.util.List;

/**
 * Clase filtro de lista and.
 */
public class FiltroListaAnd implements Filtro {
  private final List<Filtro> filtros;

  public FiltroListaAnd(List<Filtro> filtros) {
    this.filtros =  Collections.unmodifiableList(filtros);
  }

  @Override
  public List<Hecho> filtrar(List<Hecho> hechos) {
    List<Hecho> resultado = hechos;
    for (Filtro filtro : filtros) {
      resultado = filtro.filtrar(resultado);
    }
    return resultado;
  }

}