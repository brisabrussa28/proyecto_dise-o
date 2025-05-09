package ar.edu.utn.frba.dds.domain.filtro;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.origen.Origen;
import java.util.List;

public class FiltroDeOrigen extends Filtro {
  Origen origen;

  public FiltroDeOrigen(Origen origen) {
    this.origen = origen;
  }

  @Override
  public List<Hecho> filtrar(List<Hecho> hechos) {
    return hechos.stream().filter(hecho -> hecho.esDeOrigen(origen)).toList();
  }
}
