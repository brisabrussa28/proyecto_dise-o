package ar.edu.utn.frba.dds.domain.filtro;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.time.LocalDateTime;

/**
 * Clase filtro de fecha.
 */
public class FiltroDeFecha extends Filtro {
  private final LocalDateTime fecha;

  public FiltroDeFecha(LocalDateTime fecha) {
    this.fecha = fecha;
  }

  @Override
  public boolean cumple(Hecho hecho) {
    return hecho.getFechaSuceso() != null && hecho.getFechaSuceso().equals(fecha);
  }
}
