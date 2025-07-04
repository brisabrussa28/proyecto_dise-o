package ar.edu.utn.frba.dds.domain.filtro;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.List;

/**
 * Este filtro no aplica ningún criterio de filtrado, simplemente devuelve los hechos
 * tal como están.
 */
public class FiltroIdentidad implements Filtro {
  @Override
  public List<Hecho> filtrar(List<Hecho> hechos) {
    return hechos;
  }
}

