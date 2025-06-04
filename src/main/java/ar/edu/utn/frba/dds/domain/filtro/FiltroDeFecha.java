package ar.edu.utn.frba.dds.domain.filtro;

import java.time.LocalDateTime;

/**
 * Clase filtro de fecha.
 */
public class FiltroDeFecha extends Filtro {
  /**
   * Constructor de FiltroDeFecha.
   *
   * @param fecha Fecha a filtrar en los hechos.
   */

  public FiltroDeFecha(LocalDateTime fecha) {
    super(hechos -> {
      LocalDateTime referencia = fecha.toLocalDate()
                                      .atStartOfDay();
      return hechos.stream()
                   .filter(h -> {
                     LocalDateTime fechaHecho = h.getFechaSuceso()
                                                 .toLocalDate()
                                                 .atStartOfDay();
                     return fechaHecho.equals(referencia);
                   })
                   .toList();
    });
  }
}
