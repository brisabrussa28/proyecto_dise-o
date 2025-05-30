package ar.edu.utn.frba.dds.domain.filtro;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.Date;
import java.util.List;

/**
 * Clase filtro de fecha.
 */
public class FiltroDeFecha extends Filtro {
  Date fecha;

  /**
   * Constructor.
   */
  public FiltroDeFecha(Date fecha) {
    this.fecha = fecha;
  }

  @Override
  public List<Hecho> filtrar(List<Hecho> hechos) {
    return hechos.stream().filter(hecho -> hecho.esDeFecha(fecha)).toList();
  }
}
