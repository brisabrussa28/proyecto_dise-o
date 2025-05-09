package ar.edu.utn.frba.dds.domain.filtro;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.List;

/**
 * Clase filtro de etiqueta.
 */
public class FiltroDeEtiqueta extends Filtro {
  String etiqueta;

  /**
   * Constructor.
   */
  public FiltroDeEtiqueta(String etiqueta) {
    this.etiqueta = etiqueta;
  }

  @Override
  public List<Hecho> filtrar(List<Hecho> hechos) {
    return hechos.stream()
        .filter(h -> h.tieneEtiqueta(etiqueta))
        .toList();
  }
}
