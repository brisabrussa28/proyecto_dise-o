package ar.edu.utn.frba.dds.model.lector.csv.filaconverter.model.filtro.condiciones;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;

@Entity
@DiscriminatorValue("Compuesta")
public abstract class CondicionCompuesta extends Condicion {

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "condicion_padre_id") // Columna en la tabla Condicion que apunta al padre
  private List<Condicion> condiciones = new ArrayList<>();


  public CondicionCompuesta() {
  }

  public void agregarCondicion(Condicion condicion) {
    this.condiciones.add(condicion);
  }

  public void eliminar(Condicion condicion) {
    this.condiciones.remove(condicion);
  }

  public List<Condicion> getCondiciones() {
    return Collections.unmodifiableList(condiciones);
  }

  public void setCondiciones(List<Condicion> condiciones) {
    this.condiciones.clear();
    if (condiciones != null) {
      this.condiciones.addAll(condiciones);
    }
  }
}