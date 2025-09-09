package ar.edu.utn.frba.dds.domain.fuentes;

import ar.edu.utn.frba.dds.domain.Conexion.Conexion;
import ar.edu.utn.frba.dds.domain.exceptions.ConexionFuenteDemoException;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.hecho.HechoBuilder;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.origen.Origen;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class FuenteDemo extends FuenteDeCopiaLocal {

  private static final Logger logger = Logger.getLogger(FuenteDemo.class.getName());
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
    List<Hecho> nuevosHechos = new ArrayList<>();
    Map<String, Object> datos;

    try {
      while ((datos = conexion.siguienteHecho(url, ultimaActualizacion)) != null) {
        try {
          Hecho hecho = construirHechoIndividual(datos);
          nuevosHechos.add(hecho);
        } catch (ConexionFuenteDemoException e) {
          // Si un hecho individual falla, se registra y se continúa con el siguiente.
          // Esto hace que el proceso sea resiliente a datos corruptos.
          logger.warning("Se omitió un hecho de la fuente Demo por datos inválidos. Causa: "
              + e.getCause().getMessage());
        }
      }
      this.ultimaActualizacion = LocalDateTime.now();
      return nuevosHechos;
    } catch (Exception e) {
      // Este error captura problemas más grandes, como la imposibilidad de conectar con la fuente.
      throw new ConexionFuenteDemoException("Error al consultar la fuente externa de Demo", e);
    }
  }

  private Hecho construirHechoIndividual(Map<String, Object> datos) {
    try {
      String titulo = (String) datos.get("titulo");
      String descripcion = (String) datos.get("descripcion");
      String categoria = (String) datos.get("categoria");
      String direccion = (String) datos.get("direccion");
      String provincia = (String) datos.get("provincia");

      PuntoGeografico ubicacion = null;
      if (datos.get("ubicacion") instanceof Map) {
        Map<?, ?> ubicacionMap = (Map<?, ?>) datos.get("ubicacion");
        if (ubicacionMap != null && ubicacionMap.get("latitud") != null && ubicacionMap.get("longitud") != null) {
          double latitud = ((Number) ubicacionMap.get("latitud")).doubleValue();
          double longitud = ((Number) ubicacionMap.get("longitud")).doubleValue();
          ubicacion = new PuntoGeografico(latitud, longitud);
        }
      }

      LocalDateTime fechaSuceso = datos.get("fechaSuceso") != null ? LocalDateTime.parse((String) datos.get("fechaSuceso")) : null;
      LocalDateTime fechaCarga = datos.get("fechaCarga") != null ? LocalDateTime.parse((String) datos.get("fechaCarga")) : null;
      Origen fuenteOrigen = datos.get("fuenteOrigen") != null ? Origen.valueOf((String) datos.get("fuenteOrigen")) : null;

      List<String> etiquetas = new ArrayList<>();
      if (datos.get("etiquetas") instanceof List<?>) {
        for (Object o : (List<?>) datos.get("etiquetas")) {
          if (o instanceof String) {
            etiquetas.add((String) o);
          }
        }
      }

      HechoBuilder builder = new HechoBuilder()
          .conTitulo(titulo)
          .conDescripcion(descripcion)
          .conCategoria(categoria)
          .conDireccion(direccion)
          .conProvincia(provincia)
          .conUbicacion(ubicacion)
          .conFechaSuceso(fechaSuceso)
          .conFuenteOrigen(fuenteOrigen)
          .conEtiquetas(etiquetas);

      if (fechaCarga != null) {
        builder.conFechaCarga(fechaCarga);
      }

      return builder.build();

    } catch (Exception e) {
      // Se envuelve la excepción original para dar contexto claro del error.
      throw new ConexionFuenteDemoException("Error al parsear un hecho individual desde la fuente Demo", e);
    }
  }
}