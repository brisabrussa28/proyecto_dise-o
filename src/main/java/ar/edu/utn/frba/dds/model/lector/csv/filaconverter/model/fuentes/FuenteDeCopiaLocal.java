package ar.edu.utn.frba.dds.model.lector.csv.filaconverter.model.fuentes;

import ar.edu.utn.frba.dds.model.fuentes.FuenteConHechos;
import ar.edu.utn.frba.dds.model.hecho.Hecho;
import java.util.List;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

/**
 * Clase abstracta para fuentes que mantienen una copia local persistida en la base de datos.
 */
@Entity
@Table(name = "fuente_copia_local")
@PrimaryKeyJoinColumn(name = "fuente_id")
@DiscriminatorValue("COPIA_LOCAL")
public abstract class FuenteDeCopiaLocal extends FuenteConHechos {

  protected FuenteDeCopiaLocal() {
    super();
  }

  public FuenteDeCopiaLocal(String nombre) {
    super(nombre);
  }

  @Override
  public String getTipo() {
    return "COPIA_LOCAL";
  }

  /**
   * Las subclases deben implementar este método para definir de dónde
   * obtienen la información actualizada.
   */
  protected abstract List<Hecho> consultarNuevosHechos();

  public void forzarActualizacionSincrona() {
    List<Hecho> nuevosHechos = this.consultarNuevosHechos();
    if (nuevosHechos != null && !nuevosHechos.isEmpty()) {
      this.setHechos(nuevosHechos);
    }
  }

  @Override
  public String toString() {
    // Simplificado para evitar trigger de lazy loading
    return String.format("FuenteDeCopiaLocal{id=%d, nombre='%s', tipo='%s'}",
                         getId(), getNombre(), getTipo());
  }
}