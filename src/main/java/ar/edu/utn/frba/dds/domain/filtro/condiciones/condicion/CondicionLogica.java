package ar.edu.utn.frba.dds.domain.filtro.condiciones.condicion;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class CondicionLogica extends Condicion {

  public void agregarCondicion(Condicion condicion) {
  }

  public List<Condicion> getCondiciones() {
    return new ArrayList<>();
  }

  @Override
  public Map<String, Object> unMap() {
    Map<String, Object> mapa = new LinkedHashMap<>();

    // Determina la lógica ("AND" o "OR") basado en el nombre de la clase hija.
    String logica = this.getClass()
                        .getSimpleName()
                        .replace("Condicion", "")
                        .toUpperCase();
    mapa.put("logica", logica);

    // Convierte recursivamente cada sub-condición a su mapa y la agrega a una lista.
    mapa.put(
        "condiciones",
        this.getCondiciones()
            .stream()
            .map(Condicion::unMap) // Llama a aMapa() en cada sub-condición
            .collect(Collectors.toList())
    );

    return mapa;
  }
}
