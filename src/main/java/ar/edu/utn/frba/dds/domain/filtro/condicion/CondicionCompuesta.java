package ar.edu.utn.frba.dds.domain.filtro.condicion;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.ArrayList;
import java.util.List;

public abstract class CondicionCompuesta extends Condicion {
  private List<Condicion> condiciones = new ArrayList<>();

  public void agregar(Condicion condicion) {
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
}

