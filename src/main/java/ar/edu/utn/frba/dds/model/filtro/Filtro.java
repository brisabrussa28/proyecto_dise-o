package ar.edu.utn.frba.dds.model.filtro;

import ar.edu.utn.frba.dds.model.filtro.condiciones.Condicion;
import ar.edu.utn.frba.dds.model.hecho.Hecho;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.stream.Collectors;

@JsonIgnoreProperties
public class Filtro {
  private Condicion condicionRaiz;

  public Filtro(Condicion condicionRaiz) {
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

  public void setCondicion(Condicion condicion) {
    this.condicionRaiz = condicion;
  }

}
