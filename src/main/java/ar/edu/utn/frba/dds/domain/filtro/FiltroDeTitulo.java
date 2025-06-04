package ar.edu.utn.frba.dds.domain.filtro;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.origen.Origen;
import java.util.List;

/**
 * Clase de filtro de titulo.
 */
public class FiltroDeTitulo extends Filtro {
  /**
   * Constructor de FiltroDeTitulo.
   *
   * @param titulo Titulo a filtrar en los hechos.
   */
  public FiltroDeTitulo(String titulo) {
    super(hechos -> hechos.stream()
        .filter(h -> h.getTitulo().equalsIgnoreCase(titulo))
        .toList());
  }
}