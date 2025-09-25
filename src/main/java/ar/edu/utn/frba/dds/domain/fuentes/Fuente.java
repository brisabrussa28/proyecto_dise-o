package ar.edu.utn.frba.dds.domain.fuentes;

import ar.edu.utn.frba.dds.domain.geilocalizacion.ServicioGeoref;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import java.util.List;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

/**
 * Clase base para todas las fuentes de datos. Preparada para persistencia con JPA.
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_fuente", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("BASE") // Valor por defecto para la clase base.
public abstract class Fuente {

  @Id
  @GeneratedValue
  private Long id;

  protected String nombre;

  // Constructor requerido por JPA.
  protected Fuente() {
  }

  /**
   * Constructor principal de la clase fuente.
   *
   * @param nombre Nombre de la fuente.
   */
  public Fuente(String nombre) {
    this.validarFuente(nombre);
    this.nombre = nombre;
  }

  private void validarFuente(String nombre) {
    if (nombre == null || nombre.isEmpty()) {
      throw new RuntimeException("El nombre de la fuente no puede ser nulo ni vacío.");
    }
  }

  /**
   * Trae los hechos de la fuente.
   *
   * @return Lista de hechos de la fuente.
   */
  public abstract List<Hecho> obtenerHechos();

  public String getNombre() {
    return this.nombre;
  }

  public void completarProvinciasFaltantes() {
    ServicioGeoref servicio = new ServicioGeoref();
    List<Hecho> hechos = this.obtenerHechos();

    for (Hecho hecho : hechos) {
      if (hecho.getProvincia() == null || hecho.getProvincia().isBlank()) {
        PuntoGeografico ubicacion = hecho.getUbicacion();
        if (ubicacion != null) {
          String provincia = servicio.obtenerProvincia(
              ubicacion.getLatitud(),
              ubicacion.getLongitud()
          );
          if (provincia != null && !provincia.isBlank()) {
            hecho.setProvincia(provincia);
          }
        }
      }
    }
  }

}

