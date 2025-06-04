package ar.edu.utn.frba.dds.domain.filtro;

import java.time.LocalDateTime;

/**
 * Filtro por fecha de carga.
 */
public class FiltroDeFechaDeCarga extends Filtro {
  /**
   * Constructor de FiltroDeFechaDeCarga.
   *
   * @param fechaCarga Fecha de carga a filtrar en los hechos.
   */
  public FiltroDeFechaDeCarga(LocalDateTime fechaCarga) {
    super(hechos -> {
      LocalDateTime referencia = fechaCarga.toLocalDate()
                                           .atStartOfDay();
      return hechos.stream()
                   .filter(h -> {
                     LocalDateTime fechaHecho = h.getFechaCarga()
                                                 .toLocalDate()
                                                 .atStartOfDay();
                     return fechaHecho.equals(referencia);
                   })
                   .toList();
    });
  }
}

