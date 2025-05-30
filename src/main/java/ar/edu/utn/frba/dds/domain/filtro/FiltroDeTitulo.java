package ar.edu.utn.frba.dds.domain.filtro;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.origen.Origen;
import java.util.List;

/**
 * Clase de filtro de titulo.
 */
public class FiltroDeTitulo extends Filtro {
  private final String titulo;

  public FiltroDeTitulo(String titulo) {
    this.titulo = titulo;
  }

  @Override
  public boolean cumple(Hecho hecho) {
    return hecho.getTitulo() != null && hecho.getTitulo().equalsIgnoreCase(titulo);
  }
}