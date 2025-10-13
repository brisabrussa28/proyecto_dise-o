package ar.edu.utn.frba.dds.model.fuentes;

import ar.edu.utn.frba.dds.model.hecho.Hecho;
import java.util.List;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.SequenceGenerator;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_fuente", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("BASE")
public abstract class Fuente {

  @Id
  @SequenceGenerator(name = "fuente_seq", sequenceName = "fuente_sequence", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "fuente_seq")
  private Long fuente_id;

  protected String fuente_nombre;

  protected Fuente() {
  }

  public Fuente(String fuente_nombre) {
    if (fuente_nombre == null || fuente_nombre.isEmpty()) {
      throw new RuntimeException("El nombre de la fuente no puede ser nulo ni vacío.");
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
}
