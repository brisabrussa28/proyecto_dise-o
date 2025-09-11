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
  Long id;

  public Etiqueta(String nombre) {
  }
}
