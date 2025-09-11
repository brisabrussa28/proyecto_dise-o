package ar.edu.utn.frba.dds.domain.filtro.condiciones.condicion;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.LinkedHashMap;
import java.util.Map;

public class CondicionNot extends Condicion {
  private Condicion condicion;


  @Override
  public boolean evaluar(Hecho hecho) {
    if (this.condicion == null || hecho == null) {
      return false;
    }
    return !this.condicion.evaluar(hecho);
  }

  // Getters y Setters
  public Condicion getCondicion() {
    return condicion;
  }

  public void setCondicion(Condicion condicion) {
    this.condicion = condicion;
  }

  @Override
  public Map<String, Object> aMapa() {
    Map<String, Object> mapa = new LinkedHashMap<>();
    mapa.put("logica", "NOT");
    mapa.put("condicion", this.condicion.aMapa());
    return mapa;
  }

}
