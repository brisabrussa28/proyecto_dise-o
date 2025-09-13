package ar.edu.utn.frba.dds.domain.fuentes;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.serializadores.exportador.Exportador;
import ar.edu.utn.frba.dds.domain.serializadores.lector.Lector;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Clase fuente dinámica que permite agregar hechos en tiempo de ejecución.
 * Extiende FuenteDeCopiaLocal para soportar copias de seguridad y persistencia de configuración.
 */
@Entity
@DiscriminatorValue("DINAMICA")
public class FuenteDinamica extends FuenteDeCopiaLocal {

  // Constructor para JPA.
  protected FuenteDinamica() {
    super();
  }

  /**
   * Constructor de la clase FuenteDinamica.
   *
   * @param nombre         Nombre de la fuente dinámica.
   * @param rutaCopiaLocal Ruta al archivo para las copias de seguridad.
   * @param lector         Lector para manejar la persistencia de la caché.
   * @param exportador     Exportador para guardar la caché.
   */
  public FuenteDinamica(
      String nombre,
      String rutaCopiaLocal,
      Lector<Hecho> lector,
      Exportador<Hecho> exportador
  ) {
    super(nombre, rutaCopiaLocal, lector, exportador);
    // La lista cargada por el serializador puede ser inmutable,
    // así que la envolvemos en un ArrayList para asegurar que podamos agregarle hechos.
    this.cacheDeHechos = new ArrayList<>(this.cacheDeHechos);
  }

  /**
   * Agrega un hecho a la fuente dinámica y persiste la lista actualizada.
   *
   * @param hecho Hecho a agregar a la fuente dinámica.
   */
  public void agregarHecho(Hecho hecho) {
    if (this.cacheDeHechos == null) {
      this.cacheDeHechos = new ArrayList<>();
    }
    this.cacheDeHechos.add(hecho);

    // Guarda inmediatamente la copia local usando el exportador
    if (this.exportador != null) {
      this.exportador.exportar(this.cacheDeHechos, this.rutaCopiaLocal);
    }
  }

  /**
   * Implementación del mét0do abstracto para consultar nuevos hechos.
   * Para FuenteDinamica, los "nuevos hechos" son simplemente los que ya tiene en caché,
   * ya que no consulta una fuente externa.
   *
   * @return Una copia de la lista actual de hechos.
   */
  @Override
  protected List<Hecho> consultarNuevosHechos() {
    // Este mét0do es llamado por forzarActualizacionSincrona.
    // Simplemente devuelve el estado actual.
    if (this.cacheDeHechos == null) {
      return new ArrayList<>();
    }
    return new ArrayList<>(this.cacheDeHechos);
  }
}
