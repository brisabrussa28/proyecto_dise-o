package ar.edu.utn.frba.dds.model.fuentes;

import ar.edu.utn.frba.dds.model.hecho.Hecho;
import java.util.Collections;
import java.util.List;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

/**
 * Fuente dinámica que permite agregar hechos en tiempo de ejecución.
 */
@Entity
@Table(name = "fuente_dinamica")
@PrimaryKeyJoinColumn(name = "fuente_id")
@DiscriminatorValue("DINAMICA")
public class FuenteDinamica extends FuenteConHechos {

  public FuenteDinamica() {
    super();
  }

  public FuenteDinamica(String nombre) {
    super(nombre);
  }

  @Override
  public String getTipo() {
    return "DINAMICA";
  }

  @Override
  public List<Hecho> getHechos() {
    // Aprovecha la implementación base, solo asegura que no sea null
    return super.getHechos();
    if (this.hechosPersistidos == null || this.hechosPersistidos.isEmpty()) {
      return Collections.emptyList();
    }
    return this.hechosPersistidos;
  }

  public void removerHecho(Hecho hecho) {
    if (this.hechos != null && hecho != null) {
      // Remover usando iterador seguro o removeIf
      this.hechos.removeIf(h -> h.getId() != null && h.getId().equals(hecho.getId()));
    }
  }

  public void limpiarHechos() {
    if (this.hechos != null) {
      this.hechos.clear();
    }
  }

  public boolean estaVacia() {
    return this.hechos == null || this.hechos.isEmpty();
  }
}