package ar.edu.utn.frba.dds.model.filtro.condiciones;

import ar.edu.utn.frba.dds.model.hecho.Hecho;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("Cond_true")
public class CondicionTrue extends Condicion {

  public CondicionTrue() {
  }

  @Override
  public boolean evaluar(Hecho hecho) {
    return true;
  }
}
