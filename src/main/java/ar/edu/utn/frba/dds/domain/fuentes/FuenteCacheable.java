package ar.edu.utn.frba.dds.domain.fuentes;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.serviciodecopiaslocales.ServicioDeCopiasLocales;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase abstracta que encapsula la lógica de caching para una fuente de datos.
 * Ya NO maneja su propia programación de tareas. Su actualización debe ser
 * invocada desde un proceso externo.
 */
public abstract class FuenteCacheable implements Fuente {

  protected final String nombre;
  protected List<Hecho> cacheDeHechos;
  protected final ServicioDeCopiasLocales servicioDeCopiasLocales;

  public FuenteCacheable(String nombre, String jsonFilePathParaCopias) {
    this.validarFuente(nombre);
    this.nombre = nombre;
    this.servicioDeCopiasLocales = new ServicioDeCopiasLocales(jsonFilePathParaCopias);
    this.cacheDeHechos = this.servicioDeCopiasLocales.cargarCopiaHechos();
    if (this.cacheDeHechos == null) {
      this.cacheDeHechos = new ArrayList<>();
    }
  }

  protected abstract List<Hecho> consultarNuevosHechos();

  @Override
  public List<Hecho> obtenerHechos() {
    return List.copyOf(this.cacheDeHechos);
  }

  /**
   * Orquesta el proceso de actualización de forma síncrona.
   * Este es el método que será llamado por el proceso externo (ej. Crontab).
   */
  public void forzarActualizacionSincrona() {
    try {
      List<Hecho> nuevosHechos = this.consultarNuevosHechos();
      this.cacheDeHechos = nuevosHechos;
      this.servicioDeCopiasLocales.guardarCopiaHechos(this.cacheDeHechos);
    } catch (Exception e) {
      System.err.println("Error durante la actualización de la caché para " + this.getClass().getSimpleName() + ": " + e.getMessage());
    }
  }


  public String getNombre() {
    return this.nombre;
  }
}