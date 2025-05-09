package ar.edu.utn.frba.dds.domain.filtro;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.List;

/**
 * Filtro de Categoria.
 */
public class FiltroDeCategoria extends Filtro {

  String categoria;

  public FiltroDeCategoria(String categoria) {
    this.categoria = categoria;
  }

  @Override
  public List<Hecho> filtrar(List<Hecho> hechos) {
    return hechos.stream()
        .filter(h -> h.esDeCategoria(categoria))
        .toList();
  }
}
