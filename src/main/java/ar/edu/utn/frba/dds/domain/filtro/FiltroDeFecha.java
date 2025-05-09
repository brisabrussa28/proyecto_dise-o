package ar.edu.utn.frba.dds.domain.filtro;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Clase filtro de fecha.
 */
public class FiltroDeFecha extends Filtro {
  LocalDateTime fecha;

  /**
   * Constructor.
   */
  public FiltroDeFecha(LocalDateTime fecha) {
    this.fecha = fecha;
  }

  @Override
  public List<Hecho> filtrar(List<Hecho> hechos) {
    return hechos.stream().filter(hecho -> hecho.esDeFecha(fecha)).toList();
  }
}
