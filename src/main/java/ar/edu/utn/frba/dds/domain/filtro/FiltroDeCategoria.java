package ar.edu.utn.frba.dds.domain.filtro;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.List;

/**
 * Filtro por categoria.
 */
public class FiltroDeCategoria implements Filtro {
  private final String categoria;


  public FiltroDeCategoria(String categoria) {
    this.categoria = categoria;
  }

  @Override
  public List<Hecho> filtrar(List<Hecho> hechos) {
    return hechos.stream()
                 .filter(h -> h.getCategoria().equalsIgnoreCase(categoria))
                 .toList();
  }

}
