package ar.edu.utn.frba.dds.domain.hecho.etiqueta;

import javax.persistence.Embeddable;

/**
 * Etiqueta.
 */
@Embeddable
public class Etiqueta {
  public String etiqueta_nombre;

  public Etiqueta() {
  }

  public Etiqueta(String nombre) {
    this.etiqueta_nombre = nombre;
  }

  public String getNombre() {
    return this.etiqueta_nombre;
  }
}
