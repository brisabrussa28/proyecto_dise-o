package ar.edu.utn.frba.dds.model.estadisticas;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Representa un único dato estadístico, compuesto por un nombre (dimensión)
 * y un valor numérico.
 * Esta clase es inmutable.
 */
@Entity
@Table(name = "Estadistica")
public class Estadistica {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long estadistica_id;

  private String estadistica_tipo;

  @Column(name = "estadistica_nombre")
  private String estadistica_nombre;
  @Column(name = "estadistica_valor")
  private Long estadistica_valor;


  public Estadistica(String nombre, Long valor, String estadistica_tipo) {
    this.estadistica_nombre = nombre;
    this.estadistica_valor = valor;
    this.estadistica_tipo = estadistica_tipo;
  }

  public Estadistica() {
  }

  public String getNombre() {
    return estadistica_nombre;
  }

  public Long getValor() {
    return estadistica_valor;
  }

  public Long getId() {
    return this.estadistica_id;
  }

  public String getTipo() {
    return this.estadistica_tipo;
  }

  public void setId(Long id) {
    this.estadistica_id = id;
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
