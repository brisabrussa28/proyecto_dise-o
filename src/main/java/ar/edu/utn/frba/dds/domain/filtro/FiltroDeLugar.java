package ar.edu.utn.frba.dds.domain.filtro;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import java.time.LocalDateTime;
import java.util.List;

public class FiltroDeLugar extends Filtro {
  PuntoGeografico ubicacion;

  public FiltroDeLugar(PuntoGeografico ubicacion) {
    this.ubicacion = ubicacion;
  }

  @Override
  public List<Hecho> filtrar(List<Hecho> hechos) {
    return hechos.stream().filter(hecho -> hecho.esDeLugar(ubicacion)).toList();
  }
}