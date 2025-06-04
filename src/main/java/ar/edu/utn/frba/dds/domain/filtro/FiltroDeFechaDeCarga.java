package ar.edu.utn.frba.dds.domain.filtro;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class FiltroDeFechaDeCarga extends Filtro {
  /**
   * Constructor de FiltroDeFechaDeCarga.
   *
   * @param fechaCarga Fecha de carga a filtrar en los hechos.
   */
  public FiltroDeFechaDeCarga(LocalDateTime fechaCarga) {
    super(hechos -> {
      LocalDateTime referencia = fechaCarga.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
      return hechos.stream()
                   .filter(h -> {
                      // Convertir la fecha de carga del hecho a LocalDate para comparar dias (sin hora)
                     LocalDateTime fechaHecho = h.getFechaCarga().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                     return fechaHecho.equals(referencia);
                   })
                   .toList();
    });
  }
}

