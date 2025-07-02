package ar.edu.utn.frba.dds.domain.filtro;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import java.util.List;

/**
 * Clase de filtro de lugar.
 */
public class FiltroDeLugar implements Filtro {
  private final PuntoGeografico lugar;


  public FiltroDeLugar(PuntoGeografico lugar) {
    this.lugar = lugar;
  }

  @Override
  public List<Hecho> filtrar(List<Hecho> hechos) {
    return hechos.stream()
                 .filter(h -> h.getUbicacion()
                               .equals(lugar))
                 .toList();
  }
}