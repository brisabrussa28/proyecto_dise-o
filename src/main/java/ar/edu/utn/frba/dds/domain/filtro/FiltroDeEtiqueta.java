package ar.edu.utn.frba.dds.domain.filtro;

/**
 * Clase filtro de etiqueta.
 */
public class FiltroDeEtiqueta extends Filtro {
  /**
   * Constructor de FiltroDeEtiqueta.
   *
   * @param etiqueta Etiqueta a filtrar en los hechos.
   */
  public FiltroDeEtiqueta(String etiqueta) {
    super(hechos -> hechos.stream()
                          .filter(h -> h.getEtiquetas()
                                        .contains(etiqueta))
                          .toList());
  }
}