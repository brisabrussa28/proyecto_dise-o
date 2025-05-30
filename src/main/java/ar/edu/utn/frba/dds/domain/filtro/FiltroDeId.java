package ar.edu.utn.frba.dds.domain.filtro;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.List;
import java.util.UUID;

/**
 * Clase de filtro de id.
 */
public class FiltroDeId extends Filtro {
  private final UUID id;

  public FiltroDeId(UUID id) {
    this.id = id;
  }

  @Override
  public boolean cumple(Hecho hecho) {
    return hecho.getId() != null && hecho.getId().equals(id);
  }
}