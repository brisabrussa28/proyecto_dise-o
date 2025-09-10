package ar.edu.utn.frba.dds.domain.filtro.condicion;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;

public class CondicionIgualHecho implements Condicion {
  private Hecho hechoAComparar;

  public CondicionIgualHecho(Hecho hechoAComparar) {
    this.hechoAComparar = hechoAComparar;
  }

  @Override
  public boolean evaluar(Hecho hecho) {
    if (this.hechoAComparar == null || hecho == null) {
      return false;
    }
    return this.hechoAComparar.equals(hecho);
  }

  // Getters y Setters
  public Hecho getHechoAComparar() {
    return hechoAComparar;
  }

  public void setHechoAComparar(Hecho hechoAComparar) {
    this.hechoAComparar = hechoAComparar;
  }
}
