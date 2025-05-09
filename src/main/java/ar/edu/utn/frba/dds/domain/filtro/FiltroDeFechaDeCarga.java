package ar.edu.utn.frba.dds.domain.filtro;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.time.LocalDateTime;
import java.util.List;

public class FiltroDeFechaDeCarga extends Filtro {
  LocalDateTime fecha;

  public FiltroDeFechaDeCarga(LocalDateTime fecha) {
    this.fecha = fecha;
  }

  public List<Hecho> filtrarPorFechaDeCarga(List<Hecho> hechos) {
    return hechos.stream().filter(hecho -> hecho.seCargoEn(fecha)).toList();
  }
}