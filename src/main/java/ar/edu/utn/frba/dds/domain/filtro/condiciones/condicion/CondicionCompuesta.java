package ar.edu.utn.frba.dds.domain.filtro.condiciones.condicion;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class CondicionCompuesta extends Condicion {
  private List<Condicion> condiciones = new ArrayList<>();


  public void agregarCondicion(Condicion condicion) {
    this.condiciones.add(condicion);
  }

  public void eliminar(Condicion condicion) {
    this.condiciones.remove(condicion);
  }

  public List<Condicion> getCondiciones() {
    return condiciones;
  }

  public void setCondiciones(List<Condicion> condiciones) {
    this.condiciones = condiciones;
  }

  @Override
  public Map<String, Object> aMapa() {
    Map<String, Object> mapa = new LinkedHashMap<>();

    // Determina la lógica ("AND" o "OR") basado en el nombre de la clase hija.
    String logica = this.getClass()
                        .getSimpleName()
                        .replace("Condicion", "")
                        .toUpperCase();
    mapa.put("Compuesta", logica);

    // Convierte recursivamente cada sub-condición a su mapa y la agrega a una lista.
    mapa.put(
        "condiciones",
        this.getCondiciones()
            .stream()
            .map(Condicion::aMapa) // Llama a aMapa() en cada sub-condición
            .collect(Collectors.toList())
    );

    return mapa;
  }

}

