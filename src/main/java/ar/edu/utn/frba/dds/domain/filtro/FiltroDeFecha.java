package ar.edu.utn.frba.dds.domain.filtro;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.Date;
import java.util.List;

/**
 * Clase filtro de fecha.
 */
public class FiltroDeFecha extends Filtro {
  private final java.util.Date fecha;

  public FiltroDeFecha(java.util.Date fecha) {
    this.fecha = fecha;
  }

  @Override
  public boolean cumple(Hecho hecho) {
    return hecho.getFechaSuceso() != null && hecho.getFechaSuceso().equals(fecha);
  }
}
