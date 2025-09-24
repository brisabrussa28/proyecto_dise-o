package ar.edu.utn.frba.dds.domain.filtro.condiciones;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("Cond_and")
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
