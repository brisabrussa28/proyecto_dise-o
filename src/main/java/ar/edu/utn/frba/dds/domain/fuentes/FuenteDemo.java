package ar.edu.utn.frba.dds.domain.fuentes;

import ar.edu.utn.frba.dds.domain.exceptions.ConexionFuenteDemoException;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.origen.Origen;
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
  public List<Hecho> obtenerHechos() {
    return new ArrayList<>(cacheUltimosHechos); // copia defensiva
  }

  /**
   * Scheduler interno: consulta a la fuente externa una vez por hora.
   */
  public void iniciarScheduler() {
    scheduler = Executors.newSingleThreadScheduledExecutor();
    scheduler.scheduleAtFixedRate(this::actualizarHechos, 0, 1, TimeUnit.HOURS);
  }

  public void detenerScheduler() {
    if (scheduler != null && !scheduler.isShutdown()) {
      scheduler.shutdown();
    }
  }

//  public List<Hecho> construirHechosDesdeMap(Map<String, Object> datos, UUID idUsuario) {
//    List<Hecho> hechosConstruidos = new ArrayList<>();
//    List<Map<String, Object>> hechos = (List<Map<String, Object>>) datos.get("hechos");
//
//    for (Map<String, Object> hechoMap : hechos) {
//      Hecho hecho = construirHechoIndividual(hechoMap, idUsuario);
//      hechosConstruidos.add(hecho);
//    }
//
//    return hechosConstruidos;
//  }

  private Hecho construirHechoIndividual(Map<String, Object> datos) {
    String titulo = (String) datos.get("titulo");
    String descripcion = (String) datos.get("descripcion");
    String categoria = (String) datos.get("categoria");
    String direccion = (String) datos.get("direccion");

    Map<String, Object> ubicacionMap = (Map<String, Object>) datos.get("ubicacion");
    double latitud = ((Number) ubicacionMap.get("latitud")).doubleValue();
    double longitud = ((Number) ubicacionMap.get("longitud")).doubleValue();
    PuntoGeografico ubicacion = new PuntoGeografico(latitud, longitud);

    LocalDateTime fechaSuceso = LocalDateTime.parse((String) datos.get("fechaSuceso"));
    LocalDateTime fechaCarga = LocalDateTime.parse((String) datos.get("fechaCarga"));

    Origen fuenteOrigen = Origen.valueOf((String) datos.get("fuenteOrigen"));

    @SuppressWarnings("unchecked")
    List<String> etiquetas = (List<String>) datos.get("etiquetas");

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
  }

  public void actualizarHechos() {
    try {
      LocalDateTime ahora = LocalDateTime.now();

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
      }
    } catch (Exception e) {
      System.err.println("Error al consultar FuenteDemo: " + e.getMessage());
      throw new ConexionFuenteDemoException("Error al consultar FuenteDemo", e);
    }
  }


}