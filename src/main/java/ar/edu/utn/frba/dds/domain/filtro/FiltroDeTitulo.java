package ar.edu.utn.frba.dds.domain.filtro;

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
                          .filter(h -> h.getTitulo()
                                        .equalsIgnoreCase(titulo))
                          .toList());
  }
}