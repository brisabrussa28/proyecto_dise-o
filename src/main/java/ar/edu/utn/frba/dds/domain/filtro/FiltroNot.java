package ar.edu.utn.frba.dds.domain.filtro;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.ArrayList;
import java.util.List;

public class FiltroNot extends Filtro {
  public FiltroNot(Filtro filtro) {
    super(hechos -> {
      List<Hecho> originales = new ArrayList<>(hechos);
      List<Hecho> excluidos = filtro.filtrar(hechos);
      originales.removeAll(excluidos);
      return originales;
    });
  }
}