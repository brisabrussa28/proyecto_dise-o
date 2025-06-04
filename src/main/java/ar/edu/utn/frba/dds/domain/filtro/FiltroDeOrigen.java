package ar.edu.utn.frba.dds.domain.filtro;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.origen.Origen;
import java.util.List;

/**
 * Clase de filtro de origen.
 */
public class FiltroDeOrigen extends Filtro {
  /**
   * Constructor de FiltroDeOrigen.
   *
   * @param origen Origen a filtrar en los hechos.
   */


  public FiltroDeOrigen(Origen origen) {
    super(hechos -> hechos.stream()
        .filter(h -> h.getOrigen().equals(origen))
        .toList());
  }
}