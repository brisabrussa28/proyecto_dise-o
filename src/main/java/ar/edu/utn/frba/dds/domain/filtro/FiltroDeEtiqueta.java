package ar.edu.utn.frba.dds.domain.filtro;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.List;

/**
 * Clase filtro de etiqueta.
 */
public class FiltroDeEtiqueta extends Filtro {
  private final String etiqueta;

  public FiltroDeEtiqueta(String etiqueta) {
    this.etiqueta = etiqueta;
  }

  @Override
  public boolean cumple(Hecho hecho) {
    return hecho.getEtiquetas() != null && hecho.getEtiquetas().contains(etiqueta);
  }
}
