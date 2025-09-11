package ar.edu.utn.frba.dds.domain.filtro;

import ar.edu.utn.frba.dds.domain.filtro.condiciones.condicion.Condicion;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.List;
import java.util.stream.Collectors;

public class FiltroPersistente {
  private Condicion condicionRaiz;

  public FiltroPersistente(Condicion condicionRaiz) {
    this.condicionRaiz = condicionRaiz;
  }


  /**
   * Implementación del método de la interfaz Filtro.
   * Aplica la estructura de condiciones a una lista de Hechos.
   *
   * @param hechos La lista de hechos a filtrar.
   * @return Una nueva lista con los hechos que cumplen las condiciones.
   */
  public List<Hecho> filtrar(List<Hecho> hechos) {
    if (condicionRaiz == null) {
      return hechos; // Si no hay condición, no se filtra nada.
    }
    return hechos.stream()
        .filter(condicionRaiz::evaluar) // Llama al método evaluar de la condición raíz para cada hecho
        .collect(Collectors.toList());
  }

}
