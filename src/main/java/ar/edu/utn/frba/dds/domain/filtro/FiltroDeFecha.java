package ar.edu.utn.frba.dds.domain.filtro;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.Date;
import java.util.List;
import java.time.LocalDate;
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

  public FiltroDeFecha(Date fecha) {
    super(hechos -> {
      LocalDate referencia = fecha.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
      return hechos.stream()
          .filter(h -> {
            // Convertir la fecha de carga del hecho a LocalDate para comparar dias (sin hora)
            LocalDate fechaHecho = h.getFechaSuceso().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            return fechaHecho.equals(referencia);
          })
          .toList();
    });
  }
}
