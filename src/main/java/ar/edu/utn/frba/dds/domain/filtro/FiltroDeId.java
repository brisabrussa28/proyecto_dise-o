package ar.edu.utn.frba.dds.domain.filtro;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.List;

/**
 * Clase de filtro de id.
 */
public class FiltroDeId implements Filtro {
  private final String id;


  public FiltroDeId(String id) {
    this.id = id;
  }

  @Override
  public List<Hecho> filtrar(List<Hecho> hechos) {
    return hechos.stream()
                 .filter(h -> h.getId()
                               .toString()
                               .equals(id))
                 .toList();
  }
}