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
  private final String nombre;
  private final Long valor;

  public Estadistica(String nombre, Long valor) {
    this.nombre = nombre;
    this.valor = valor;
  }

  public String getNombre() {
    return nombre;
  }

  public Long getValor() {
    return valor;
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
    return Objects.equals(nombre, that.nombre) && Objects.equals(valor, that.valor);
  }

  @Override
  public int hashCode() {
    return Objects.hash(nombre, valor);
  }

  @Override
  public String toString() {
    return "Estadistica{" + "nombre='" + nombre + '\'' + ", valor=" + valor + '}';
  }
}
