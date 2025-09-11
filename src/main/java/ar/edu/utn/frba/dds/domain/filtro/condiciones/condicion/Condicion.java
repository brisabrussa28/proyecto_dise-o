package ar.edu.utn.frba.dds.domain.filtro.condiciones.condicion;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.LinkedHashMap;
import java.util.Map;

public class Condicion {
  /**
   * Método abstracto que cada tipo de condición debe implementar para
   * determinar si un Hecho cumple con el criterio.
   *
   * @param hecho El Hecho a evaluar.
   * @return true si el Hecho cumple la condición, false en caso contrario.
   */
  public boolean evaluar(Hecho hecho) {
    return true;
  }

  public String aJsonString() {
    // Se usa la librería Gson para convertir el mapa a un String JSON.
    Gson gson = new GsonBuilder().setPrettyPrinting()
                                 .create();
    return gson.toJson(this.aMapa());
  }

  public Map<String, Object> aMapa() {
    return new LinkedHashMap<>();
  }
}
