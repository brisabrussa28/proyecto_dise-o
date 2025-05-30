package ar.edu.utn.frba.dds.domain.filtro;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.List;

/**
 * Clase filtro de dirección.
 */
import java.util.List;

public class FiltroDeDireccion extends Filtro {

  private final String direccion;

  public FiltroDeDireccion(String direccion) {
    this.direccion = direccion;
  }

  @Override
  public boolean cumple(Hecho hecho) {
    return hecho.getDireccion() != null && hecho.getDireccion().equalsIgnoreCase(direccion);
  }
}

