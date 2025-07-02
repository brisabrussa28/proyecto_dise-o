package ar.edu.utn.frba.dds.domain.filtro;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.List;

/**
 * Filtro por direccion de un hecho.
 */
public class FiltroDeDireccion implements Filtro {
  private final String direccion;


  public FiltroDeDireccion(String direccion) {
    this.direccion = direccion;
  }

  @Override
  public List<Hecho> filtrar(List<Hecho> hechos) {
    return hechos.stream()
                 .filter(h -> h.getDireccion()
                               .equalsIgnoreCase(direccion))
                 .toList();
  }
}

