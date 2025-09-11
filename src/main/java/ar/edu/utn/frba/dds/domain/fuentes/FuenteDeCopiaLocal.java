package ar.edu.utn.frba.dds.domain.fuentes;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.serializadores.Serializador;
import java.util.Collections;
import java.util.List;

/**
 * Clase abstracta que encapsula la lógica de caching para una fuente de datos,
 * utilizando un Serializador para persistir la copia local.
 */
public abstract class FuenteDeCopiaLocal implements Fuente {

  protected final String nombre;
  protected List<Hecho> cacheDeHechos;
  protected final Serializador<Hecho> serializador;
  protected final String rutaCopiaLocal;

  /**
   * Constructor que recibe un serializador para manejar la copia local.
   *
   * @param nombre         Nombre de la fuente.
   * @param rutaCopiaLocal Ruta del archivo para guardar/cargar la copia (e.g., "copia.json").
   * @param serializador   Serializador para manejar la persistencia de la caché.
   */
  public FuenteDeCopiaLocal(String nombre, String rutaCopiaLocal, Serializador<Hecho> serializador) {
    this.validarFuente(nombre);
    this.nombre = nombre;
    this.rutaCopiaLocal = rutaCopiaLocal;
    this.serializador = serializador;
    this.cacheDeHechos = this.serializador.importar(this.rutaCopiaLocal);
  }

  /**
   * Lógica para consultar los nuevos hechos desde el origen real (API, etc.).
   * @return La lista de hechos actualizada.
   */
  protected abstract List<Hecho> consultarNuevosHechos();

  @Override
  public List<Hecho> obtenerHechos() {
    return Collections.unmodifiableList(this.cacheDeHechos);
  }

  /**
   * Actualiza la fuente de forma síncrona: consulta nuevos hechos,
   * actualiza la caché en memoria y persiste la caché en disco usando el serializador.
   */
  public void forzarActualizacionSincrona() {
    List<Hecho> nuevosHechos = this.consultarNuevosHechos();
    this.cacheDeHechos = nuevosHechos;
    this.serializador.exportar(this.cacheDeHechos, this.rutaCopiaLocal);
  }

  public String getNombre() {
    return this.nombre;
  }
}
