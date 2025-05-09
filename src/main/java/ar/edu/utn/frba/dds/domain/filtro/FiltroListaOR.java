package ar.edu.utn.frba.dds.domain.filtro;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.List;

public class FiltroListaOR extends Filtro {
  List<Filtro> filtros;

  public FiltroListaOR(List<Filtro> filtros) {
    this.filtros = filtros;
  }

  @Override
  public List<Hecho> filtrar(List<Hecho> hechos) {
    return filtros.stream()
        .flatMap(filtro -> filtro.filtrar(hechos).stream())
        .distinct()  // eliminar duplicados si corresponde
        .toList();
  }
}
