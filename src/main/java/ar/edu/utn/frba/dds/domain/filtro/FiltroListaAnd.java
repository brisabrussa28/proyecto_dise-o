package ar.edu.utn.frba.dds.domain.filtro;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase filtro de lista and.
 */
public class FiltroListaAnd extends Filtro {
  public FiltroListaAnd(List<Filtro> filtros) {
    super(hechos -> {
      List<Hecho> resultado = hechos;
      for (Filtro filtro : filtros) {
        resultado = filtro.filtrar(resultado);
      }
      return resultado;
    });
  }
}