package ar.edu.utn.frba.dds.domain.hecho.etiqueta;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Etiqueta.
 */
@Entity
public class Etiqueta {
  @Id
  @GeneratedValue
  Long etiqueta_id;

  String etiqueta_nombre;

  public Etiqueta(String nombreEtiqueta) {
    this.etiqueta_nombre = nombreEtiqueta;
  }

  public String getEtiqueta_nombre() {
    return this.etiqueta_nombre;
  }
}
