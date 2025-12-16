package ar.edu.utn.frba.dds.model.lector.csv.filaconverter.model.fuentes;

import ar.edu.utn.frba.dds.model.hecho.Hecho;
import java.util.List;
import java.util.Objects;
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

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "fuente_tipo", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("BASE")
public abstract class Fuente {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  protected Long fuente_id;

  @Column(name = "fuente_nombre", nullable = false)
  protected String fuente_nombre;

  protected Fuente() {
    // Constructor protegido para JPA
  }

  public Fuente(String fuente_nombre) {
    if (fuente_nombre == null || fuente_nombre.trim().isEmpty()) {
      throw new IllegalArgumentException("El nombre de la fuente no puede ser nulo ni vacío.");
    }
    this.fuente_nombre = fuente_nombre;
  }

  public abstract List<Hecho> getHechos();

  public String getNombre() {
    return this.fuente_nombre;
  }

  public Long getId() {
    return this.fuente_id;
  }

  public void setNombre(String nombre) {
    if (nombre == null || nombre.trim().isEmpty()) {
      throw new IllegalArgumentException("El nombre no puede ser nulo ni vacío.");
    }
    this.fuente_nombre = nombre;
  }

  public String getTipo() {
    return "BASE";
  }

  /**
   * Obtiene la cantidad de hechos en esta fuente.
   * CUIDADO: Invocar este método puede disparar la carga diferida (Lazy Loading)
   * de todos los hechos o consultas a APIs externas.
   */
  public int getCantidadHechos() {
    List<Hecho> hechos = getHechos();
    return hechos != null ? hechos.size() : 0;
  }

  @Override
  public String toString() {
    // IMPORTANTE: No llamar a getCantidadHechos() aquí para evitar recursión infinita
    // o cargas masivas de base de datos al loguear el objeto.
    return String.format("Fuente{id=%d, nombre='%s', tipo='%s'}",
                         fuente_id, fuente_nombre, getTipo());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Fuente)) return false;
    Fuente fuente = (Fuente) o;
    // Usar getters para asegurar compatibilidad con proxies de Hibernate
    return getId() != null && getId().equals(fuente.getId());
  }

  @Override
  public int hashCode() {
    return getId() != null ? getId().hashCode() : 0;
  }
}