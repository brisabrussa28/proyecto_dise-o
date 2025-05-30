package ar.edu.utn.frba.dds.domain.filtro;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.origen.Origen;
import java.util.List;

/**
 * Clase de filtro de origen.
 */
public class FiltroDeOrigen extends Filtro {
  private final Origen origen;

  public FiltroDeOrigen(Origen origen) {
    this.origen = origen;
  }

  @Override
  public boolean cumple(Hecho hecho) {
    return hecho.getOrigen() != null && hecho.getOrigen().equals(origen);
  }
}