package ar.edu.utn.frba.dds.domain.filtro;

/**
 * Filtro por direccion de un hecho.
 * */
public class FiltroDeDireccion extends Filtro {
  /**
   * Constructor de FiltroDeDireccion.
   *
   * @param direccion Dirección a filtrar en los hechos.
   */
  public FiltroDeDireccion(String direccion) {
    super(hechos -> hechos.stream()
                          .filter(h -> h.getDireccion()
                                        .equalsIgnoreCase(direccion))
                          .toList());
  }
}

