package ar.edu.utn.frba.dds.domain.fuentes;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.serviciodecopiaslocales.ServicioDeCopiasLocales;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Clase abstracta que encapsula la lógica de caching para una fuente de datos.
 * Maneja una copia local de los hechos, la persistencia en un archivo JSON y
 * la actualización periódica de los datos a través de un scheduler.
 */
public abstract class FuenteCacheable implements Fuente {

  String nombre;
  protected List<Hecho> cacheDeHechos;
  protected final ServicioDeCopiasLocales servicioDeCopiasLocales;
  private ScheduledExecutorService scheduler;

  /**
   * Constructor para una fuente cacheable.
   *
   * @param jsonFilePathParaCopias Ruta al archivo JSON para guardar y cargar la caché.
   */
  public FuenteCacheable(String nombre, String jsonFilePathParaCopias) {
    this.nombre = nombre;
    this.servicioDeCopiasLocales = new ServicioDeCopiasLocales(jsonFilePathParaCopias);
    this.cacheDeHechos = this.servicioDeCopiasLocales.cargarCopiaHechos();
    if (this.cacheDeHechos == null) {
      this.cacheDeHechos = new ArrayList<>();
    }
    System.out.println(this.getClass().getSimpleName() + ": Hechos cargados inicialmente desde JSON (" + this.cacheDeHechos.size() + ").");
  }

  /**
   * Método que las clases hijas deben implementar para definir cómo se obtienen
   * los hechos desde la fuente original (ej. una API externa, agregando otras fuentes, etc.).
   *
   * @return Una lista de nuevos hechos para actualizar la caché.
   */
  protected abstract List<Hecho> consultarNuevosHechos();

  /**
   * Devuelve los hechos desde la caché interna. No consulta la fuente original.
   *
   * @return Una copia defensiva e inmutable de la lista de hechos en caché.
   */
  @Override
  public List<Hecho> obtenerHechos() {
    // Devuelve una copia inmutable para máxima seguridad. Requiere Java 10+.
    // Para Java 8, usar: Collections.unmodifiableList(new ArrayList<>(this.cacheDeHechos));
    return List.copyOf(this.cacheDeHechos);
  }

  /**
   * Inicia el proceso de actualización periódica.
   * NO es invocado en el constructor para permitir control externo (ej. en la app principal).
   *
   * @param periodo El intervalo entre actualizaciones.
   * @param unidad  La unidad de tiempo para el periodo (ej. TimeUnit.HOURS).
   */
  public void iniciarScheduler(long periodo, TimeUnit unidad) {
    if (scheduler == null || scheduler.isShutdown()) {
      this.scheduler = Executors.newSingleThreadScheduledExecutor();
      // La tarea se ejecuta inmediatamente y luego en intervalos fijos.
      this.scheduler.scheduleAtFixedRate(this::forzarActualizacionSincrona, 0, periodo, unidad);
    }
  }

  /**
   * Detiene las actualizaciones programadas.
   */
  public void detenerScheduler() {
    if (this.scheduler != null && !this.scheduler.isShutdown()) {
      this.scheduler.shutdown();
    }
  }

  /**
   * Orquesta el proceso de actualización de forma síncrona.
   * Consulta los nuevos hechos, actualiza la caché en memoria y guarda la nueva versión en disco.
   * Este método es público para poder ser invocado directamente en los tests.
   */
  public void forzarActualizacionSincrona() {
    try {
      System.out.println(this.getClass().getSimpleName() + ": Iniciando actualización de la caché...");
      List<Hecho> nuevosHechos = this.consultarNuevosHechos();
      this.cacheDeHechos = nuevosHechos;
      this.servicioDeCopiasLocales.guardarCopiaHechos(this.cacheDeHechos);
      System.out.println(this.getClass().getSimpleName() + ": Caché actualizada y guardada con " + nuevosHechos.size() + " hechos.");
    } catch (Exception e) {
      System.err.println("Error durante la actualización de la caché para " + this.getClass().getSimpleName() + ": " + e.getMessage());
      // Opcional: Manejar la excepción de forma más robusta.
    }
  }
}