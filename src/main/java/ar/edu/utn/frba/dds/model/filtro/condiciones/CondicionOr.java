package ar.edu.utn.frba.dds.model.filtro.condiciones;

import ar.edu.utn.frba.dds.model.hecho.Hecho;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("Cond_or")
public class CondicionOr extends CondicionCompuesta {

  @Override
  public boolean evaluar(Hecho hecho) {
    if (this.getCondiciones()
            .isEmpty()) {
      return true;
    }
    return this.getCondiciones()
               .stream()
               .anyMatch(c -> c.evaluar(hecho));
  }
}
