package ar.edu.utn.frba.dds.domain.fuentes;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.serializadores.Lector.Lector;
import ar.edu.utn.frba.dds.domain.serializadores.exportador.Exportador;
import java.util.Collections;
import java.util.List;
import javax.persistence.ManyToMany;

/**
 * Clase abstracta que encapsula la lógica de caching para una fuente de datos,
 * utilizando un lector para persistir la copia local.
 */
public abstract class FuenteDeCopiaLocal extends Fuente {
  @ManyToMany
  protected List<Hecho> cacheDeHechos;

  protected final Lector<Hecho> lector;
  protected final Exportador<Hecho> exportador ;

  protected final String rutaCopiaLocal;

  /**
   * Constructor que recibe un lector para manejar la copia local.
   *
   * @param nombre         Nombre de la fuente.
   * @param rutaCopiaLocal Ruta del archivo para guardar/cargar la copia (e.g., "copia.json").
   * @param lector   lector para manejar la persistencia de la caché.
   */
  public FuenteDeCopiaLocal(String nombre, String rutaCopiaLocal, Lector<Hecho> lector, Exportador<Hecho> exportador) {
    super(nombre);
    this.rutaCopiaLocal = rutaCopiaLocal;
    this.lector = lector;
    this.exportador = exportador;
    this.cacheDeHechos = this.lector.importar(this.rutaCopiaLocal);
  }

  /**
   * Lógica para consultar los nuevos hechos desde el origen real (API, etc.).
   *
   * @return La lista de hechos actualizada.
   */
  protected abstract List<Hecho> consultarNuevosHechos();

  @Override
  public List<Hecho> obtenerHechos() {
    return Collections.unmodifiableList(this.cacheDeHechos);
  }

  /**
   * Actualiza la fuente de forma síncrona: consulta nuevos hechos,
   * actualiza la caché en memoria y persiste la caché en disco usando el lector.
   */
  public void forzarActualizacionSincrona() {
    List<Hecho> nuevosHechos = this.consultarNuevosHechos();
    this.cacheDeHechos = nuevosHechos;
    this.exportador.exportar(this.cacheDeHechos, this.rutaCopiaLocal);
  }

  public String getNombre() {
    return this.nombre;
  }
}
