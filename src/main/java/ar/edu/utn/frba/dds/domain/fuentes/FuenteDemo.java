import ar.edu.utn.frba.dds.domain.fuentes.FuenteProxy;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;
import java.time.LocalDateTime;

/**
 * FuenteDemo representa una fuente proxy que se conecta a un sistema externo
 * ficticio utilizando una biblioteca externa provista (clase Conexion).
 * Es stateful y consulta nuevos hechos cada una hora mediante programación interna.
 * Si se consulta antes de la hora, devuelve una copia del resultado anterior (cache).
 */


public class FuenteDemo extends FuenteProxy {
  private Conexion conexion;
  private URL url;
  private LocalDateTime ultimaActualizacion;
  private ScheduledExecutorService scheduler;
  private List<Hecho> cacheUltimosHechos = new ArrayList<>();

  public FuenteDemo(URL url, Conexion conexion) {
    super("FuenteDemo");
    this.url = url;
    this.conexion = conexion;
    this.ultimaActualizacion = null;
    iniciarScheduler();
  }

  /**
   * Devuelve los hechos más recientes cacheados. No consulta la fuente externa.
   */
  @Override
  public List<Hecho> obtenerNuevosHechos() {
    return new ArrayList<>(cacheUltimosHechos); // copia defensiva
  }

  /**
   * Scheduler interno: consulta a la fuente externa una vez por hora.
   */
  private void iniciarScheduler() {
    scheduler = Executors.newSingleThreadScheduledExecutor();
    scheduler.scheduleAtFixedRate(() -> {
      try {
        LocalDateTime ahora = LocalDateTime.now();

        if (ultimaActualizacion == null || ahora.isAfter(ultimaActualizacion.plusHours(1))) {
          List<Hecho> nuevosHechos = new ArrayList<>();
          Map<String, Object> datos;

          while ((datos = conexion.siguienteHecho(url, ultimaActualizacion)) != null) {

            nuevosHechos.add(datos);



          }

          cacheUltimosHechos = nuevosHechos;
          ultimaActualizacion = ahora;

          System.out.println("FuenteDemo actualizó hechos (" + nuevosHechos.size() + ").");
        }
      } catch (Exception e) {
        System.err.println("Error al consultar FuenteDemo: " + e.getMessage());
      }
    }, 0, 1, TimeUnit.HOURS);
  }

  public void detenerScheduler() {
    if (scheduler != null && !scheduler.isShutdown()) {
      scheduler.shutdown();
    }
  }
}