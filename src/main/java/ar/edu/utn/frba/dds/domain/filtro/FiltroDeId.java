package ar.edu.utn.frba.dds.domain.filtro;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.List;
import java.util.UUID;

/**
 * Clase de filtro de id.
 */
public class FiltroDeId extends Filtro {
  public FiltroDeId(String id) {
    super(hechos -> hechos.stream()
                          .filter(h -> h.getId().toString().equals(id))
                          .toList());
  }
}