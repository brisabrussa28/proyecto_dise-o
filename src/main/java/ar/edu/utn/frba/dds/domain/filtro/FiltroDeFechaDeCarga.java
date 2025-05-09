package ar.edu.utn.frba.dds.domain.filtro;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Clase filtro de fecha de carga.
 */
public class FiltroDeFechaDeCarga extends Filtro {
  LocalDateTime fecha;

  /**
   * Constructor.
   */
  public FiltroDeFechaDeCarga(LocalDateTime fecha) {
    this.fecha = fecha;
  }

  @Override
  public List<Hecho> filtrar(List<Hecho> hechos) {
    return hechos.stream().filter(hecho -> hecho.seCargoEl(fecha)).toList();
  }
}