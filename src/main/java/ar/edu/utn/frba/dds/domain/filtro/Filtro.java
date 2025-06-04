package ar.edu.utn.frba.dds.domain.filtro;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.List;

/**
 * Clase base para filtros de hechos.
 */
public class Filtro {
  private final Operacion operacion;

  /**
   * Constructor de Filtro.
   *
   * @param operacion Operación que define cómo se filtran los hechos.
   */
  public Filtro(Operacion operacion) {
    this.operacion = operacion;
  }

  /**
   * Filtra una lista de hechos utilizando la operación definida.
   *
   * @param hechos Lista de hechos a filtrar.
   * @return Lista de hechos filtrados.
   */
  public List<Hecho> filtrar(List<Hecho> hechos) {
    return operacion.ejecutar(hechos);
  }

}
