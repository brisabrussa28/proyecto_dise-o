package ar.edu.utn.frba.dds.domain.filtro;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;

public class FiltroNot extends Filtro {

  private final Filtro filtro;

  public FiltroNot(Filtro filtro) {
    this.filtro = filtro;
  }

  @Override
  public boolean cumple(Hecho hecho) {
    return !filtro.cumple(hecho);
  }
}