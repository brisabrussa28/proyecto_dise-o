package ar.edu.utn.frba.dds.domain.fuentes;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Clase fuente dinámica.
 */
public class FuenteDinamica extends FuenteCacheable {

  // Los hechos ahora se gestionan a través de cacheDeHechos en FuenteCacheable

  /**
   * Constructor de la clase FuenteDinamica.
   *
   * @param nombre                 Nombre de la fuente dinámica.
   * @param jsonFilePathParaCopias Ruta al archivo JSON para copias de seguridad.
   */
  public FuenteDinamica(String nombre, String jsonFilePathParaCopias) {
    super(nombre, jsonFilePathParaCopias);
  }

  /**
   * Agrega un hecho a la fuente dinámica.
   *
   * @param hecho Hecho a agregar a la fuente dinámica.
   */
  public void agregarHecho(Hecho hecho) {
    this.cacheDeHechos.add(hecho);
    this.servicioDeCopiasLocales.guardarCopiaLocalJson(this.cacheDeHechos);
  }


  /**
   * Obtiene los hechos de la fuente dinámica.
   *
   * @return Lista de hechos de la fuente dinámica (copia inmutable).
   */
  @Override
  public List<Hecho> obtenerHechos() {
    return Collections.unmodifiableList(this.cacheDeHechos);
  }

  /**
   * Para FuenteDinamica, los "nuevos hechos" son simplemente los que ya tiene en caché,
   * ya que no consulta una fuente externa de forma periódica.
   * Este método es llamado por forzarActualizacionSincrona() de FuenteCacheable.
   *
   * @return Una copia actualizada de la lista de hechos.
   */
  @Override
  protected List<Hecho> consultarNuevosHechos() {
    return new ArrayList<>(this.cacheDeHechos);
  }
}
