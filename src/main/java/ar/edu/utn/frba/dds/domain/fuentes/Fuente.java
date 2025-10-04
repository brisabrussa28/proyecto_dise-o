package ar.edu.utn.frba.dds.domain.fuentes;

import ar.edu.utn.frba.dds.domain.geolocalizacion.ServicioGeoref;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;

import java.util.List;
import javax.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_fuente", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("BASE")
public abstract class Fuente {

  @Id
  @GeneratedValue
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
