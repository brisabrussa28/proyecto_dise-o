package ar.edu.utn.frba.dds.model.lector.csv.filaconverter.model.estadisticas;

import ar.edu.utn.frba.dds.model.lector.csv.filaconverter.model.coleccion.Coleccion;
import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Representa un único dato estadístico, compuesto por un nombre (dimensión)
 * y un valor numérico.
 * Esta clase es inmutable.
 */
@Entity
@Table(name = "Estadistica")
public class Estadistica {

  //[id_stats]
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long estadistica_id;

  //[nombre del grupo --> nombre provincia/hora]
  private String estadistica_grupo;
  //[valor_cantidad --> hechos, hechos reportados, solicitudes, spam]
  private Long estadistica_cantidad;
  //[categoria_nombre]
  private String estadistica_categoria;
  //[coleccion_id]
  @ManyToOne
  @JoinColumn(name = "coleccion_id", nullable = true)
  private Coleccion estadistica_coleccion;
  //[tipo_stats]
  private String estadistica_tipo;


  //debo cambiar el constructor
  public Estadistica(String grupo, Long cantidad, String categoria, Coleccion coleccion, String tipo) {
    this.estadistica_grupo = grupo;
    this.estadistica_cantidad = cantidad;
    this.estadistica_tipo = tipo;
    this.estadistica_categoria = categoria;
    this.estadistica_coleccion = coleccion;
  }

  public Estadistica() {
  }

  // --- Getters ---
  public Long getId() { return estadistica_id; }
  public String getGrupo() { return estadistica_grupo; }
  public Long getCantidad() { return estadistica_cantidad; }
  public String getTipo() { return estadistica_tipo; }
  public String getCategoria() { return estadistica_categoria; }
  public Coleccion getColeccion() { return estadistica_coleccion; }

  // --- Setters ---
  public void setColeccion(Coleccion coleccion) {
    this.estadistica_coleccion = coleccion;
  }
  public void setId(Long id) {
    this.estadistica_id = id;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Estadistica that = (Estadistica) o;
    return Objects.equals(estadistica_grupo, that.estadistica_grupo)
        && Objects.equals(estadistica_cantidad, that.estadistica_cantidad)
        && Objects.equals(estadistica_categoria, that.estadistica_categoria)
        && Objects.equals(estadistica_tipo, that.estadistica_tipo)
        && Objects.equals(estadistica_coleccion, that.estadistica_coleccion);
  }

  @Override
  public int hashCode() {
    return Objects.hash(estadistica_tipo, estadistica_cantidad);
  }

  @Override
  public String toString() {
    return "Estadistica{" + "nombre='" + estadistica_tipo + '\'' + ", valor=" + estadistica_cantidad + '}';
  }
}
