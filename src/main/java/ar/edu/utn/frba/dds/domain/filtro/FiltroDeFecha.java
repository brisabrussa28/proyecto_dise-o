package ar.edu.utn.frba.dds.domain.filtro;

import java.time.LocalDateTime;
import java.time.ZoneId;

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
      LocalDateTime referencia = fecha.atZone(ZoneId.systemDefault())
                                      .toLocalDateTime();
      return hechos.stream()
                   .filter(h -> {
                     // Convertir la fecha de carga del hecho a LocalDate para comparar dias (sin hora)
                     LocalDateTime fechaHecho = h.getFechaSuceso()
                                                 .atZone(ZoneId.systemDefault())
                                                 .toLocalDateTime();
                     return fechaHecho.equals(referencia);
                   })
                   .toList();
    });
  }
}
