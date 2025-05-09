package ar.edu.utn.frba.dds.domain.filtro;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.origen.Origen;
import java.util.List;

public class FiltroDeOrigen {
  Origen origen;

  public FiltroDeOrigen(Origen origen) {
    this.origen = origen;
  }
  public List<Hecho> filtrarPorOrigen(List<Hecho> hechos) {
    return hechos.stream().filter(hecho -> hecho.esDeOrigen(origen)).toList();
  }
}
