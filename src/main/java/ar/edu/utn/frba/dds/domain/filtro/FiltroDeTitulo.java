package ar.edu.utn.frba.dds.domain.filtro;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.origen.Origen;
import java.util.List;

/**
 * Clase de filtro de titulo.
 */
public class FiltroDeTitulo extends Filtro {
  String titulo;

  /**
   * Constructor.
   */
  public FiltroDeTitulo(String titulo) {
    this.titulo = titulo;
  }

  @Override
  public List<Hecho> filtrar(List<Hecho> hechos) {
    return hechos.stream().filter(hecho -> hecho.esDeTitulo(titulo)).toList();
  }
}