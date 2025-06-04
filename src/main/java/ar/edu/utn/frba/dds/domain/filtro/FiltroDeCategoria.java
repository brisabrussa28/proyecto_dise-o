package ar.edu.utn.frba.dds.domain.filtro;

public class FiltroDeCategoria extends Filtro {
  /**
   * Constructor de FiltroDeCategoria.
   *
   * @param categoria Categoría a filtrar en los hechos.
   */
  public FiltroDeCategoria(String categoria) {
    super(hechos -> hechos.stream()
                          .filter(h -> h.getCategoria()
                                        .equalsIgnoreCase(categoria))
                          .toList());
  }
}
