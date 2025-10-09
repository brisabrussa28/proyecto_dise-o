package ar.edu.utn.frba.dds.domain.estadisticas;

import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

/**
 * Representa un único dato estadístico, compuesto por un nombre (dimensión)
 * y un valor numérico.
 * Esta clase es inmutable.
 */
@Entity
public class Estadistica {

  @Id
  @SequenceGenerator(name = "estadistica_seq", sequenceName = "estadistica_sequence", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "estadistica_seq")
  Long estadistica_id;
  private final String estadistica_nombre;
  private final Long estadistica_valor;

  public Estadistica(String nombre, Long valor) {
    this.estadistica_nombre = nombre;
    this.estadistica_valor = valor;
  }

  public String getNombre() {
    return estadistica_nombre;
  }

  public Long getValor() {
    return estadistica_valor;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Estadistica that = (Estadistica) o;
    return Objects.equals(estadistica_nombre, that.estadistica_nombre) && Objects.equals(
        estadistica_valor, that.estadistica_valor);
  }

  @Override
  public int hashCode() {
    return Objects.hash(estadistica_nombre, estadistica_valor);
  }

  @Override
  public String toString() {
    return "Estadistica{" + "nombre='" + estadistica_nombre + '\'' + ", valor=" + estadistica_valor + '}';
  }
}
