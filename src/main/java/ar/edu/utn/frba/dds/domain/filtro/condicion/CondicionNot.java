package ar.edu.utn.frba.dds.domain.filtro.condicion;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;

public class CondicionNot implements Condicion {
  private Condicion condicion;

  public CondicionNot(Condicion condicion) {
    this.condicion = condicion;
  }

  @Override
  public boolean evaluar(Hecho hecho) {
    if (this.condicion == null || hecho == null) {
      return false;
    }
    return !this.condicion.evaluar(hecho);
  }

  // Getters y Setters
  public Condicion getCondicionANegar() {
    return condicion;
  }

  public void setondicionANegar(Condicion condicion) {
    this.condicion = condicion;
  }
}
