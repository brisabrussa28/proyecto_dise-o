package ar.edu.utn.frba.dds.model.fuentes;

import ar.edu.utn.frba.dds.model.hecho.Hecho;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;

/**
 * Clase abstracta para fuentes que mantienen una copia local persistida en la base de datos.
 * Las subclases deben implementar la lógica para consultar los datos frescos.
 */
@Entity
@DiscriminatorValue("COPIA_LOCAL")
public abstract class FuenteDeCopiaLocal extends FuenteConHechos {

  // Relación persistente específica para copia local
  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  @JoinColumn(name = "copia_local_fuente_id")
  protected List<Hecho> copiaLocalDeHechos = new ArrayList<>();

  protected FuenteDeCopiaLocal() {
    super();
  }

  public FuenteDeCopiaLocal(String nombre) {
    super(nombre);
  }

  /**
   * Las subclases deben implementar este método para definir de dónde
   * obtienen la información actualizada (ej: una API, otra fuente, etc.).
   *
   * @return Una lista con los hechos más recientes.
   */
  protected abstract List<Hecho> consultarNuevosHechos();

  @Override
  public List<Hecho> getHechos() {
    if (this.copiaLocalDeHechos == null || this.copiaLocalDeHechos.isEmpty()) {
      return Collections.emptyList();
    }
    return Collections.unmodifiableList(this.copiaLocalDeHechos);
  }

  /**
   * Ejecuta la lógica para buscar nuevos hechos y actualiza la copia local
   * persistida en la base de datos.
   */
  public void forzarActualizacionSincrona() {
    List<Hecho> nuevosHechos = this.consultarNuevosHechos();
    if (nuevosHechos != null && !nuevosHechos.isEmpty()) {
      this.copiaLocalDeHechos.clear();
      this.copiaLocalDeHechos.addAll(nuevosHechos);
    }
  }

  /**
   * Setter para la copia local.
   */
  public void setCopiaLocalDeHechos(List<Hecho> hechos) {
    if (this.copiaLocalDeHechos == null) {
      this.copiaLocalDeHechos = new ArrayList<>();
    } else {
      this.copiaLocalDeHechos.clear();
    }
    if (hechos != null) {
      this.copiaLocalDeHechos.addAll(hechos);
    }
  }
}