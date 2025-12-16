package ar.edu.utn.frba.dds.model.lector.csv.filaconverter.model.filtro.condiciones;

import ar.edu.utn.frba.dds.model.filtro.condiciones.Condicion;
import ar.edu.utn.frba.dds.model.hecho.Hecho;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
@DiscriminatorValue("Cond_not")
public class CondicionNot extends ar.edu.utn.frba.dds.model.filtro.condiciones.Condicion {

  @ManyToOne(cascade = CascadeType.ALL)
  private ar.edu.utn.frba.dds.model.filtro.condiciones.Condicion condicion;

  public CondicionNot() {
  }

  public CondicionNot(ar.edu.utn.frba.dds.model.filtro.condiciones.Condicion condicion) {
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

  public ar.edu.utn.frba.dds.model.filtro.condiciones.Condicion getCondicion() {
    return condicion;
  }

  public void setCondicion(Condicion condicion) {
    this.condicion = condicion;
  }
}