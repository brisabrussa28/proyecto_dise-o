package ar.edu.utn.frba.dds.domain.filtro;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;

import java.util.List;

public class FiltroDeCategoria extends Filtro {

  private final String categoria;

  public FiltroDeCategoria(String categoria) {
    this.categoria = categoria;
  }

  @Override
  public boolean cumple(Hecho hecho) {
    return hecho.getCategoria() != null && hecho.getCategoria().equalsIgnoreCase(categoria);
  }
}
