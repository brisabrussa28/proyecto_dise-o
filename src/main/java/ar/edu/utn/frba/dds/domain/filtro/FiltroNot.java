package ar.edu.utn.frba.dds.domain.filtro;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.ArrayList;
import java.util.List;

/**
 * Filtro excluyente.
 */
public class FiltroNot implements Filtro {
  private final Filtro filtro;

  public FiltroNot(Filtro filtro) {
    this.filtro = filtro;
  }

  @Override
  public List<Hecho> filtrar(List<Hecho> hechos) {
    List<Hecho> originales = new ArrayList<>(hechos);
    List<Hecho> excluidos = filtro.filtrar(hechos);
    originales.removeAll(excluidos);
    return originales;
  }

}