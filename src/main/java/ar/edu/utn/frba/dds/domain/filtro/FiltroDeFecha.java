package ar.edu.utn.frba.dds.domain.filtro;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Clase filtro de fecha.
 */
public class FiltroDeFecha implements Filtro {
  private final LocalDateTime fecha;


  public FiltroDeFecha(LocalDateTime fecha) {
    this.fecha = fecha;
  }

  @Override
  public List<Hecho> filtrar(List<Hecho> hechos) {
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
  }
}
