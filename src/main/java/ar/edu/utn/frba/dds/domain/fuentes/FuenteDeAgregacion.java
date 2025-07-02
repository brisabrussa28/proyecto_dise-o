package ar.edu.utn.frba.dds.domain.fuentes;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.serviciodecopiaslocales.ServicioDeCopiasLocales; // Import the local copy service
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * Servicio de Agregación.
 * Permite agregar múltiples fuentes y obtener los hechos combinados de todas ellas.
 * Ahora cuenta con un mecanismo para guardar copias locales de los hechos agregados
 * de forma periódica, utilizando ServicioDeCopiasLocales.
 */
public class FuenteDeAgregacion implements Fuente {
  private final List<Fuente> fuentesCargadas;
  private final ServicioDeCopiasLocales servicioDeCopiasLocales; // Instance of local copy service
  private ScheduledExecutorService scheduler; // Scheduler for periodic updates
  private List<Hecho> cacheHechosAgregados = new ArrayList<>(); // Cache for aggregated facts

  /**
   * Constructor del Servicio de Agregación.
   *
   * @param nombre Nombre del servicio de agregación
   * @param jsonFilePathParaCopias Ruta del archivo JSON para las copias locales
   */
  public FuenteDeAgregacion(String nombre, String jsonFilePathParaCopias) {
    this.fuentesCargadas = new ArrayList<>();
    this.servicioDeCopiasLocales = new ServicioDeCopiasLocales(jsonFilePathParaCopias);
    // Load existing facts from JSON on startup
    this.cacheHechosAgregados = servicioDeCopiasLocales.cargarCopiaHechos();
    iniciarScheduler(); // Start the scheduler for periodic updates
  }

  /**
   * Agrega una fuente al servicio de agregación.
   *
   * @param fuente Fuente a agregar
   */
  public void agregarFuente(Fuente fuente) {
    this.fuentesCargadas.add(fuente);
  }

  /**
   * Obtiene los hechos de todas las fuentes cargadas.
   * Esta implementación ahora devuelve los hechos desde la caché interna.
   * La caché se actualiza periódicamente.
   *
   * @return Lista de hechos combinados de todas las fuentes
   */
  @Override
  public List<Hecho> obtenerHechos() {
    return new ArrayList<>(cacheHechosAgregados); // Return a defensive copy of the cached facts
  }

  /**
   * Obtiene las fuentes cargadas en el servicio de agregación.
   *
   * @return Lista de fuentes cargadas
   */
  public List<Fuente> getFuentesCargadas() {
    return new ArrayList<>(fuentesCargadas);
  }

  /**
   * Inicia el scheduler para actualizar periódicamente los hechos agregados
   * y guardar una copia local.
   * La actualización se realizará cada hora.
   */
  public void iniciarScheduler() {
    scheduler = Executors.newSingleThreadScheduledExecutor();
    // Schedule the update and save task to run immediately, then every hour
    scheduler.scheduleAtFixedRate(this::actualizarHechosAgregadosYGuardarCopia, 0, 1, TimeUnit.HOURS);
  }

  /**
   * Detiene el scheduler, impidiendo futuras actualizaciones programadas.
   */
  public void detenerScheduler() {
    if (scheduler != null && !scheduler.isShutdown()) {
      scheduler.shutdown();
    }
  }

  /**
   * Actualiza la caché de hechos agregados consultando todas las fuentes cargadas
   * y luego guarda una copia local de estos hechos.
   * Este método es público para permitir su invocación directa en tests.
   */
  public void actualizarHechosAgregadosYGuardarCopia() { // Changed from private to public
    System.out.println("FuenteDeAgregacion: Iniciando actualización de hechos agregados...");

    this.cacheHechosAgregados = fuentesCargadas.stream()
        .flatMap(fuente -> fuente.obtenerHechos().stream())
        .collect(Collectors.toList());

    // Save the updated aggregated facts to a local JSON copy
    servicioDeCopiasLocales.guardarCopiaHechos(cacheHechosAgregados);
  }
}
