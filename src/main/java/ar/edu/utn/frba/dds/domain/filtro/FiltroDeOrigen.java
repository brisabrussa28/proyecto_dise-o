package ar.edu.utn.frba.dds.domain.filtro;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.origen.Origen;
import java.util.List;

/**
 * Clase de filtro de origen.
 */
public class FiltroDeOrigen implements Filtro {
  private final Origen origen;


  public FiltroDeOrigen(Origen origen) {
    this.origen = origen;
  }

  @Override
  public List<Hecho> filtrar(List<Hecho> hechos) {
    return hechos.stream()
                 .filter(h -> h.getOrigen()
                               .equals(origen))
                 .toList();
  }
}