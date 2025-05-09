package ar.edu.utn.frba.dds.domain.filtro;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.time.LocalDateTime;
import java.util.List;

public class FiltroDeFecha extends Filtro {
  LocalDateTime fecha;

  public FiltroDeFecha(LocalDateTime fecha) {
    this.fecha = fecha;
  }
  public List<Hecho> filtrarPorFecha(List<Hecho> hechos) {
    return hechos.stream().filter(hecho -> hecho.esDeFecha(fecha)).toList();
  }
}
