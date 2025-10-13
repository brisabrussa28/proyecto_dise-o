package ar.edu.utn.frba.dds.model.filtro.condiciones;

import ar.edu.utn.frba.dds.model.hecho.Hecho;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_condicion")
public abstract class Condicion {
  @Id
  @GeneratedValue
  Long id;

  /**
   * Método abstracto que cada tipo de condición debe implementar para
   * determinar si un Hecho cumple con el criterio.
   *
   * @param hecho El Hecho a evaluar.
   * @return true si el Hecho cumple la condición, false en caso contrario.
   */
  public boolean evaluar(Hecho hecho) {
    return false;
  }

}