package ar.edu.utn.frba.dds.domain.filtro.condiciones.condicion;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;

public class CondicionAnd extends CondicionCompuesta {

  @Override
  public boolean evaluar(Hecho hecho) {
    if (this.getCondiciones()
            .isEmpty()) {
      return true;
    }
    return this.getCondiciones()
               .stream()
               .allMatch(c -> c.evaluar(hecho));
  }
}
