package ar.edu.utn.frba.dds.domain.filtro;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Filtro por fecha de carga.
 */
public class FiltroDeFechaDeCarga implements Filtro {
  private final LocalDateTime fechaCarga;


  public FiltroDeFechaDeCarga(LocalDateTime fechaCarga) {
    this.fechaCarga = fechaCarga;
  }

  @Override
  public List<Hecho> filtrar(List<Hecho> hechos) {
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
  }
}

