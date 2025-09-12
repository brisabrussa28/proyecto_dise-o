package ar.edu.utn.frba.dds.domain.fuentes;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.List;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

/**
 * Clase fuente.
 */

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_fuente", discriminatorType = DiscriminatorType.STRING)
public class Fuente {
  @Id
  @GeneratedValue
  private Long id;
  protected final String nombre;

  /**
   * Constructor de la clase fuente.
   *
   * @param nombre Nombre de la fuente.
   */
  public Fuente(String nombre) {
    this.validarFuente(nombre);
    this.nombre = nombre;
  }

  private void validarFuente(String nombre) {
    if (nombre == null || nombre.isEmpty()) {
      throw new RuntimeException("El nombre de la fuente no puede ser nulo ni vacío.");
    }
  }

  /**
   * Obtiene los hechos de la fuente.
   *
   * @return Lista de hechos de la fuente.
   */
  public List<Hecho> obtenerHechos() {
    return List.of();
  }


}