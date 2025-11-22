package ar.edu.utn.frba.dds.model.fuentes.apis;

import ar.edu.utn.frba.dds.model.exceptions.ConexionFuenteDemoException;
import ar.edu.utn.frba.dds.model.fuentes.apis.conexion.Conexion;
import ar.edu.utn.frba.dds.model.hecho.Hecho;
import ar.edu.utn.frba.dds.model.hecho.HechoBuilder;
import ar.edu.utn.frba.dds.model.hecho.Origen;
import ar.edu.utn.frba.dds.model.hecho.etiqueta.Etiqueta;
import ar.edu.utn.frba.dds.model.info.PuntoGeografico;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Implementación del Adapter que sabe cómo comunicarse con la fuente Demo.
 */
public class AdapterDemo implements FuenteAdapter {
  private static final Logger logger = Logger.getLogger(AdapterDemo.class.getName());
  private final Conexion conexion;
  private final URL url;
  private LocalDateTime ultimaActualizacion;

  public AdapterDemo(Conexion conexion, URL url) {
    this.conexion = conexion;
    this.url = url;
    this.ultimaActualizacion = null;
  }

  @Override
  public List<Hecho> consultarHechos() throws IOException {
    List<Hecho> nuevosHechos = new ArrayList<>();
    Map<String, Object> datos;

    try {
      while ((datos = conexion.siguienteHecho(url, ultimaActualizacion)) != null) {
        try {
          Hecho hecho = construirHechoIndividual(datos);
          nuevosHechos.add(hecho);
        } catch (ConexionFuenteDemoException e) {
          logger.warning("Se omitió este hecho por datos inválidos");
        }
      }
      this.ultimaActualizacion = LocalDateTime.now();
      return nuevosHechos;
    } catch (Exception e) {
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
        if (ubicacionMap != null && ubicacionMap.get("latitud") != null && ubicacionMap.get(
            "longitud") != null) {
          double latitud = ((Number) ubicacionMap.get("latitud")).doubleValue();
          double longitud = ((Number) ubicacionMap.get("longitud")).doubleValue();
          ubicacion = new PuntoGeografico(latitud, longitud);
        }
      }

      LocalDateTime fechaSuceso = datos.get("fechaSuceso") != null
                                  ? LocalDateTime.parse((String) datos.get("fechaSuceso")) :
                                  null;
      LocalDateTime fechaCarga = datos.get("fechaCarga") != null
                                 ? LocalDateTime.parse((String) datos.get("fechaCarga")) :
                                 null;
      Origen fuenteOrigen = datos.get("fuenteOrigen") != null
                            ? Origen.valueOf((String) datos.get("fuenteOrigen")) :
                            null;

      List<Etiqueta> etiquetas = new ArrayList<>();
      if (datos.get("etiquetas") instanceof List<?>) {
        for (Object o : (List<?>) datos.get("etiquetas")) {
          if (o instanceof String) {
            etiquetas.add(new Etiqueta((String) o));
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
      throw new ConexionFuenteDemoException(
          "Error al parsear un hecho individual desde la fuente Demo",
          e
      );
    }
  }
}

