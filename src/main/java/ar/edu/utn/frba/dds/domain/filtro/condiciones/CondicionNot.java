package ar.edu.utn.frba.dds.domain.filtro.condiciones;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
@DiscriminatorValue("Cond_not")
public class CondicionNot extends Condicion {

  @ManyToOne(cascade = CascadeType.ALL)
  private Condicion condicion;

  public CondicionNot() {
  }

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

  // --- Getters y Setters ---

  public Condicion getCondicion() {
    return condicion;
  }

  public void setCondicion(Condicion condicion) {
    this.condicion = condicion;
  }
}