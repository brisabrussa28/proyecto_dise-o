package ar.edu.utn.frba.dds.domain.filtro;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.List;

/**
 * Clase de filtro de titulo.
 */
public class FiltroDeTitulo implements Filtro {
  private final String titulo;


  public FiltroDeTitulo(String titulo) {
    this.titulo = titulo;
  }

  @Override
  public List<Hecho> filtrar(List<Hecho> hechos) {
    return hechos.stream()
                 .filter(h -> h.getTitulo()
                               .equalsIgnoreCase(titulo))
                 .toList();
  }
}