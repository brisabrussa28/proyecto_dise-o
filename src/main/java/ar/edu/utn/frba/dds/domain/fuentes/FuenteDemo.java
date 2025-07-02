package ar.edu.utn.frba.dds.domain.fuentes;

import ar.edu.utn.frba.dds.domain.exceptions.ConexionFuenteDemoException;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.origen.Origen;
import ar.edu.utn.frba.dds.domain.serviciodecopiaslocales.ServicioDeCopiasLocales; // Import the new service
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * FuenteDemo representa una fuente proxy que se conecta a un sistema externo
 * ficticio utilizando una biblioteca externa provista (clase Conexion).
 * Es stateful y consulta nuevos hechos cada una hora mediante programación interna.
 * Si se consulta antes de la hora, devuelve una copia del resultado anterior (cache).
 * Ahora colabora con ServicioDeCopiasLocales para guardar copias JSON.
 */
public class FuenteDemo implements Fuente {
  private final Conexion conexion;
  private final URL url;
  private LocalDateTime ultimaActualizacion;
  private ScheduledExecutorService scheduler;
  private List<Hecho> cacheUltimosHechos = new ArrayList<>();

  // Instancia del servicio de copias locales
  private final ServicioDeCopiasLocales servicioDeCopiasLocales;

  public FuenteDemo(URL url, Conexion conexion, String jsonFilePathParaCopias) {
    this.url = url;
    this.conexion = conexion;
    this.ultimaActualizacion = null;
    // Initialize the ServicioDeCopiasLocales with the provided file path
    this.servicioDeCopiasLocales = new ServicioDeCopiasLocales(jsonFilePathParaCopias);
    iniciarScheduler();
    // Load existing facts from JSON on startup
    this.cacheUltimosHechos = servicioDeCopiasLocales.cargarCopiaHechos();
    System.out.println("FuenteDemo: Hechos cargados inicialmente desde JSON (" + cacheUltimosHechos.size() + ").");
  }

  /**
   * Returns the most recently cached facts. Does not query the external source.
   */
  @Override
  public List<Hecho> obtenerHechos() {
    return new ArrayList<>(cacheUltimosHechos); // defensive copy
  }

  /**
   * Internal scheduler: queries the external source once per hour
   * and then notifies ServicioDeCopiasLocales to save the copy.
   */
  public void iniciarScheduler() {
    scheduler = Executors.newSingleThreadScheduledExecutor();
    // Schedule to update facts and save to JSON immediately, then every hour
    scheduler.scheduleAtFixedRate(this::actualizarHechosYGuardarCopia, 0, 1, TimeUnit.HOURS);
  }

  public void detenerScheduler() {
    if (scheduler != null && !scheduler.isShutdown()) {
      scheduler.shutdown();
    }
  }

  /**
   * Updates facts from the external source and then passes the list to
   * ServicioDeCopiasLocales for saving.
   */
  public void actualizarHechosYGuardarCopia() {
    try {
      LocalDateTime ahora = LocalDateTime.now();

      // The update logic remains the same for FuenteDemo
      if (ultimaActualizacion == null || ahora.isAfter(ultimaActualizacion.plusHours(1))) {
        List<Hecho> nuevosHechos = new ArrayList<>();
        Map<String, Object> datos;

        while ((datos = conexion.siguienteHecho(url, ultimaActualizacion)) != null) {
          Hecho hecho = construirHechoIndividual(datos);
          nuevosHechos.add(hecho);
        }

        cacheUltimosHechos = nuevosHechos;
        ultimaActualizacion = ahora;

        System.out.println("FuenteDemo actualizó hechos (" + nuevosHechos.size() + ").");

        // After updating its cache, it notifies ServicioDeCopiasLocales to save the copy
        servicioDeCopiasLocales.guardarCopiaHechos(cacheUltimosHechos);
      }
    } catch (Exception e) {
      System.err.println("Error al consultar FuenteDemo: " + e.getMessage());
      throw new ConexionFuenteDemoException("Error al consultar FuenteDemo", e);
    }
  }

  private Hecho construirHechoIndividual(Map<String, Object> datos) {
    try {
      String titulo = (String) datos.get("titulo");
      String descripcion = (String) datos.get("descripcion");
      String categoria = (String) datos.get("categoria");
      String direccion = (String) datos.get("direccion");

      if (titulo == null || descripcion == null || categoria == null || direccion == null) {
        throw new IllegalArgumentException("Datos obligatorios faltantes");
      }

      Object ubicacionObj = datos.get("ubicacion");
      double latitud = 0;
      double longitud = 0;
      if (ubicacionObj instanceof Map<?, ?> ubicacionMap) {
        latitud = ((Number) ubicacionMap.get("latitud")).doubleValue();
        longitud = ((Number) ubicacionMap.get("longitud")).doubleValue();
      } else {
        throw new IllegalArgumentException("Ubicación inválida");
      }
      PuntoGeografico ubicacion = new PuntoGeografico(latitud, longitud);

      LocalDateTime fechaSuceso = LocalDateTime.parse((String) datos.get("fechaSuceso"));
      LocalDateTime fechaCarga = LocalDateTime.parse((String) datos.get("fechaCarga"));

      Origen fuenteOrigen = Origen.valueOf((String) datos.get("fuenteOrigen"));

      Object etiquetasObj = datos.get("etiquetas");
      List<String> etiquetas = new ArrayList<>();
      if (etiquetasObj instanceof List) {
        for (Object o : (List<?>) etiquetasObj) {
          etiquetas.add(String.valueOf(o));
        }
      }

      return new Hecho(
          titulo,
          descripcion,
          categoria,
          direccion,
          ubicacion,
          fechaSuceso,
          fechaCarga,
          fuenteOrigen,
          etiquetas
      );
    } catch (Exception e) {
      throw new ConexionFuenteDemoException("Error al construir Hecho individual", e);
    }
  }
}