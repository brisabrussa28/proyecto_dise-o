package ar.edu.utn.frba.dds.model.fuentes;

import ar.edu.utn.frba.dds.model.hecho.Hecho;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

@Entity
@Table(name = "Fuente")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "fuente_tipo", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("BASE")
public abstract class Fuente {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "fuente_id")
  private Long fuente_id;

  @Column(name = "fuente_nombre", nullable = false)
  protected String fuente_nombre;

  protected Fuente() {
  }

  public Fuente(String fuente_nombre) {
    if (fuente_nombre == null || fuente_nombre.trim().isEmpty()) {
      throw new IllegalArgumentException("El nombre de la fuente no puede ser nulo ni vacío.");
    }
    this.fuente_nombre = fuente_nombre.trim();
  }

  /**
   * Obtiene los hechos de esta fuente.
   * Las subclases deben implementar este método según su tipo.
   */
  public abstract List<Hecho> getHechos();

  /**
   * Obtiene el nombre de la fuente.
   */
  public String getNombre() {
    return this.fuente_nombre;
  }

  /**
   * Obtiene el ID de la fuente.
   */
  public Long getId() {
    return this.fuente_id;
  }

  /**
   * Establece el nombre de la fuente.
   */
  public void setNombre(String nombre) {
    if (nombre == null || nombre.trim().isEmpty()) {
      throw new IllegalArgumentException("El nombre de la fuente no puede ser nulo ni vacío.");
    }
    this.fuente_nombre = nombre.trim();
  }

  /**
   * Obtiene el tipo de fuente en formato legible.
   */
  public String getTipo() {
    if (this instanceof FuenteDinamica) {
      return "Dinámica";
    } else if (this instanceof FuenteEstatica) {
      return "Estática";
    } else if (this instanceof FuenteExternaAPI) {
      return "API Externa";
    } else if (this instanceof FuenteDeAgregacion) {
      return "Agregación";
    } else if (this instanceof FuenteDeCopiaLocal) {
      return "Copia Local";
    }
    return "Desconocida";
  }

  /**
   * Obtiene el discriminador de tipo para la base de datos.
   */
  public String getTipoDiscriminador() {
    if (this instanceof FuenteDinamica) {
      return "DINAMICA";
    } else if (this instanceof FuenteEstatica) {
      return "ESTATICA";
    } else if (this instanceof FuenteExternaAPI) {
      return "API_EXTERNA";
    } else if (this instanceof FuenteDeAgregacion) {
      return "AGREGACION";
    } else if (this instanceof FuenteDeCopiaLocal) {
      return "COPIA_LOCAL";
    }
    return "BASE";
  }

  /**
   * Obtiene la cantidad de hechos en esta fuente.
   */
  public int getCantidadHechos() {
    List<Hecho> hechos = getHechos();
    return hechos != null ? hechos.size() : 0;
  }

  @Override
  public String toString() {
    return String.format("Fuente{id=%d, nombre='%s', tipo='%s', hechos=%d}",
                         fuente_id, fuente_nombre, getTipo(), getCantidadHechos());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Fuente)) return false;
    Fuente fuente = (Fuente) o;
    return fuente_id != null && fuente_id.equals(fuente.fuente_id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}