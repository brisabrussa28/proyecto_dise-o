package ar.edu.utn.frba.dds.domain.filtro;

public class FiltroIdentidad extends Filtro {
  /**
   * Constructor de FiltroIdentidad.
   * Este filtro no aplica ningún criterio de filtrado, simplemente devuelve los hechos tal como están.
   */
  public FiltroIdentidad() {
    super(hechos -> hechos);
  }
}
