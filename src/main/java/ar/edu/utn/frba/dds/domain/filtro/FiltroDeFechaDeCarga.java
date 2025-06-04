package ar.edu.utn.frba.dds.domain.filtro;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;

import java.util.Date;
import java.util.List;
import java.time.LocalDate;
import java.time.ZoneId;

public class FiltroDeFechaDeCarga extends Filtro {
  /**
   * Constructor de FiltroDeFechaDeCarga.
   *
   * @param fechaCarga Fecha de carga a filtrar en los hechos.
   */
  public FiltroDeFechaDeCarga(Date fechaCarga) {
    super(hechos -> {
      LocalDate referencia = fechaCarga.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
      return hechos.stream()
          .filter(h -> {
            // Convertir la fecha de carga del hecho a LocalDate para comparar dias (sin hora)
            LocalDate fechaHecho = h.getFechaCarga().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            return fechaHecho.equals(referencia);
          })
          .toList();
    });
  }
}

