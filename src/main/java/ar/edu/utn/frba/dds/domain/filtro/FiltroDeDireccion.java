package ar.edu.utn.frba.dds.domain.filtro;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.List;

/**
 * Clase filtro de dirección.
 */
import java.util.List;

public class FiltroDeDireccion extends Filtro {
  /**
   * Constructor de FiltroDeDireccion.
   *
   * @param direccion Dirección a filtrar en los hechos.
   */
  public FiltroDeDireccion(String direccion) {
    super(hechos -> hechos.stream()
        .filter(h -> h.getDireccion().equalsIgnoreCase(direccion))
        .toList());
  }
}

