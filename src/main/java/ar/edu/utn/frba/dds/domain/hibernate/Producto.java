package ar.edu.utn.frba.dds.domain.hibernate;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "productos")
public class Producto {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "nombre_producto", length = 100, nullable = false)
  private String nombre;

  @Column(name = "precio_unitario")
  private double precio;

  // Hibernate necesita un constructor vacío para poder instanciar el objeto.
  public Producto() {
  }

  // Constructor para facilitar la creación de nuevos productos en el código.
  public Producto(String nombre, double precio) {
    this.nombre = nombre;
    this.precio = precio;
  }

  // --- Getters y Setters ---

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public double getPrecio() {
    return precio;
  }

  public void setPrecio(double precio) {
    this.precio = precio;
  }

  @Override
  public String toString() {
    return "Producto{" +
        "id=" + id +
        ", nombre='" + nombre + '\'' +
        ", precio=" + precio +
        '}';
  }
}