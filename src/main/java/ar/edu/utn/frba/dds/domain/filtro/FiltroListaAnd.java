package ar.edu.utn.frba.dds.domain.filtro;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.List;

public class FiltroListaAnd extends Filtro {
  List<Filtro>  filtros;

  public FiltroListaAnd(List<Filtro> filtros) {
    this.filtros = filtros;
  }

  @Override
  public List<Hecho> filtrar(List<Hecho> hechos) {
    return filtros.stream()
        .reduce(hechos,
            (hechosFiltrados, filtro) -> filtro.filtrar(hechosFiltrados),
            (hechos1, hechos2) -> hechos1); // este combiner no importa porque es secuencial
  }
}

