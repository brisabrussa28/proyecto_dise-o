package ar.edu.utn.frba.dds.domain.filtro;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class FiltroDeFechaDeCarga extends Filtro {

  private final Date fecha;

  public FiltroDeFechaDeCarga(Date fecha) {
    this.fecha = fecha;
  }

  @Override
  public boolean cumple(Hecho hecho) {
    Date fechaCarga = hecho.getFechaCarga();
    if (fechaCarga == null || fecha == null) return false;

    LocalDate fechaLocalCarga = Instant.ofEpochMilli(fechaCarga.getTime())
        .atZone(ZoneId.systemDefault())
        .toLocalDate();
    LocalDate fechaLocalFiltro = Instant.ofEpochMilli(fecha.getTime())
        .atZone(ZoneId.systemDefault())
        .toLocalDate();

    return fechaLocalCarga.isEqual(fechaLocalFiltro);
  }
}


