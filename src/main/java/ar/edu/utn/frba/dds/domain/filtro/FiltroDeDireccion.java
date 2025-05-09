package ar.edu.utn.frba.dds.domain.filtro;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.List;

/**
 * Clase filtro de dirección.
 */
public class FiltroDeDireccion extends Filtro {
  String direccion;

  /**
   * Constructor.
   */
  public FiltroDeDireccion(String direccion) {
    this.direccion = direccion;
  }

  @Override
  public List<Hecho> filtrar(List<Hecho> hechos) {
    return hechos.stream().filter(hecho -> hecho.sucedioEn(direccion)).toList();
  }
}