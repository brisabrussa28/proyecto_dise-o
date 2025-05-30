package ar.edu.utn.frba.dds.domain.filtro;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Clase de filtro de lugar.
 */
public class FiltroDeLugar extends Filtro {
  private final PuntoGeografico lugar;

  public FiltroDeLugar(PuntoGeografico lugar) {
    this.lugar = lugar;
  }

  @Override
  public boolean cumple(Hecho hecho) {
    return hecho.getUbicacion() != null && hecho.getUbicacion().equals(lugar);
  }
}