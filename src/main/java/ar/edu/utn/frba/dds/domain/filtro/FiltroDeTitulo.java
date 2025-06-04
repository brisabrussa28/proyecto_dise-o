package ar.edu.utn.frba.dds.domain.filtro;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.origen.Origen;
import java.util.List;

/**
 * Clase de filtro de titulo.
 */
public class FiltroDeTitulo extends Filtro {
  public FiltroDeTitulo(String titulo) {
    super(hechos -> hechos.stream()
                          .filter(h -> h.getTitulo().equalsIgnoreCase(titulo))
                          .toList());
  }
}