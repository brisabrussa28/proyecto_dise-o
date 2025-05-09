package ar.edu.utn.frba.dds.domain.filtro;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.List;
import java.util.UUID;

/**
 * Clase de filtro de id.
 */
public class FiltroDeId extends Filtro {
  UUID id;

  /**
   * Constructor.
   */
  public FiltroDeId(UUID id) {
    this.id = id;
  }

  @Override
  public List<Hecho> filtrar(List<Hecho> hechos) {
    return hechos.stream()
        .filter(hecho -> hecho.esElMismo(id))
        .toList();
  }
}