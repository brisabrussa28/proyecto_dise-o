package ar.edu.utn.frba.dds.domain.filtro;

/**
 * Clase de filtro de id.
 */
public class FiltroDeId extends Filtro {
  /**
   * Constructor de FiltroDeId.
   *
   * @param id Identificador del hecho a filtrar.
   */
  public FiltroDeId(String id) {
    super(hechos -> hechos.stream()
                          .filter(h -> h.getId()
                                        .toString()
                                        .equals(id))
                          .toList());
  }
}