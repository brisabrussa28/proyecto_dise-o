package ar.edu.utn.frba.dds.domain.fuentes;

import ar.edu.utn.frba.dds.domain.Conexion.Conexion;
import ar.edu.utn.frba.dds.domain.exceptions.ConexionFuenteDemoException;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.origen.Origen;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FuenteDemo extends FuenteDeCopiaLocal {

  private final Conexion conexion;
  private final URL url;
  private LocalDateTime ultimaActualizacion;

  public FuenteDemo(String nombre, URL url, Conexion conexion, String jsonFilePathParaCopias) {
    super(nombre, jsonFilePathParaCopias);
    this.url = url;
    this.conexion = conexion;
    this.ultimaActualizacion = null;
  }

  @Override
  protected List<Hecho> consultarNuevosHechos() {
    try {
      List<Hecho> nuevosHechos = new ArrayList<>();
      Map<String, Object> datos;

      while ((datos = conexion.siguienteHecho(url, ultimaActualizacion)) != null) {
        Hecho hecho = construirHechoIndividual(datos);
        nuevosHechos.add(hecho);
      }

      this.ultimaActualizacion = LocalDateTime.now();
      return nuevosHechos;

    } catch (Exception e) {
      throw new ConexionFuenteDemoException("Error al consultar la fuente externa de Demo", e);
    }
  }

// Java
private Hecho construirHechoIndividual(Map<String, Object> datos) {
  try {
    String titulo = (String) datos.get("titulo");
    String descripcion = (String) datos.get("descripcion");
    String categoria = (String) datos.get("categoria");
    String direccion = (String) datos.get("direccion");
    String provincia = (String) datos.get("provincia"); // <-- Added

    if (titulo == null || descripcion == null || categoria == null || direccion == null || provincia == null) {
      throw new IllegalArgumentException("Datos obligatorios faltantes en el hecho recibido.");
    }

    Map<?, ?> ubicacionMap = (Map<?, ?>) datos.get("ubicacion");
    if (ubicacionMap == null || ubicacionMap.get("latitud") == null || ubicacionMap.get("longitud") == null) {
      throw new IllegalArgumentException("Ubicación faltante o incompleta en el hecho recibido.");
    }
    double latitud = ((Number) ubicacionMap.get("latitud")).doubleValue();
    double longitud = ((Number) ubicacionMap.get("longitud")).doubleValue();
    PuntoGeografico ubicacion = new PuntoGeografico(latitud, longitud);

    LocalDateTime fechaSuceso = LocalDateTime.parse((String) datos.get("fechaSuceso"));
    LocalDateTime fechaCarga = LocalDateTime.parse((String) datos.get("fechaCarga"));
    Origen fuenteOrigen = Origen.valueOf((String) datos.get("fuenteOrigen"));

    List<String> etiquetas = new ArrayList<>();
    Object etiquetasObj = datos.get("etiquetas");
    if (etiquetasObj instanceof List<?>) {
      for (Object o : (List<?>) etiquetasObj) {
        if (o instanceof String) {
          etiquetas.add((String) o);
        }
      }
    }

    // Add provincia to constructor
    return new Hecho(titulo, descripcion, categoria, direccion, provincia, ubicacion, fechaSuceso, fechaCarga, fuenteOrigen, etiquetas);
  } catch (Exception e) {
    throw new ConexionFuenteDemoException("Error al parsear un hecho individual desde la fuente Demo", e);
  }
}

}