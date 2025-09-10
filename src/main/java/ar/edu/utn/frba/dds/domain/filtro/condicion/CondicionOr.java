package ar.edu.utn.frba.dds.domain.filtro.condicion;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;

public class CondicionOr extends CondicionCompuesta {

  @Override
  public boolean evaluar(Hecho hecho) {
    if (this.getCondiciones().isEmpty()) {
      return true;
    }
    return this.getCondiciones().stream().anyMatch(c -> c.evaluar(hecho));
  }
}
