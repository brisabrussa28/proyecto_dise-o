package ar.edu.utn.frba.dds.domain.fuentes;

import ar.edu.utn.frba.dds.domain.geolocalizacion.ServicioGeoref;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
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
import javax.persistence.SequenceGenerator;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_fuente", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("BASE")
public abstract class Fuente {

  @Id
  @SequenceGenerator(name = "fuente_seq", sequenceName = "fuente_sequence", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "fuente_seq")
  @Column(name = "fuente_id")
  private Long id;

  protected String nombre;

  protected Fuente() {
  }

  public Fuente(String nombre) {
    if (nombre == null || nombre.isEmpty()) {
      throw new RuntimeException("El nombre de la fuente no puede ser nulo ni vacío.");
    }
    this.nombre = nombre;
  }

  public abstract List<Hecho> obtenerHechos();

  public String getNombre() {
    return this.nombre;
  }

}
