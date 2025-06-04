package ar.edu.utn.frba.dds.domain.filtro;

/**
 * Este filtro no aplica ningún criterio de filtrado, simplemente devuelve los hechos
 * tal como están.
 */
public class FiltroIdentidad extends Filtro {
  /**
   * Constructor de FiltroIdentidad.
   */
  public FiltroIdentidad() {
    super(hechos -> hechos);
  }
}
