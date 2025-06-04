package ar.edu.utn.frba.dds.domain.filtro;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.time.LocalDateTime;

public class FiltroDeFechaDeCarga extends Filtro {

  private final LocalDateTime fecha;

  public FiltroDeFechaDeCarga(LocalDateTime fecha) {
    this.fecha = fecha;
  }

  @Override
  public boolean cumple(Hecho hecho) {
    LocalDateTime fechaCarga = hecho.getFechaCarga();
    if (fechaCarga == null || fecha == null) {
      return false;
    }

    int horaCarga = fechaCarga.getHour();
    int minutoCarga = fechaCarga.getMinute();
    int horaFiltro = fecha.getHour();
    int minutoFiltro = fecha.getMinute();

    return horaCarga == horaFiltro
        && minutoCarga == minutoFiltro;
  }
}


