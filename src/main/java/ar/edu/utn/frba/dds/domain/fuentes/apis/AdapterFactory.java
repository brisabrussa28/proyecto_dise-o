package ar.edu.utn.frba.dds.domain.fuentes.apis;

import ar.edu.utn.frba.dds.domain.fuentes.apis.Conexion.Conexion;
import ar.edu.utn.frba.dds.domain.fuentes.apis.serviciometamapa.HechoQuerys;
import ar.edu.utn.frba.dds.domain.fuentes.apis.serviciometamapa.ServicioMetaMapa;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Factory dedicada a la creación de instancias de FuenteAdapter
 * a partir de una configuración JSON.
 */
public class AdapterFactory {

  /**
   * Crea un FuenteAdapter a partir de su nodo de configuración JSON.
   *
   * @param adapterNode El nodo JSON con la configuración del adaptador.
   * @return Una instancia de FuenteAdapter configurada.
   */
  public FuenteAdapter create(JsonNode adapterNode) {
    String tipo = adapterNode.path("tipo")
                             .asText()
                             .toUpperCase();
    switch (tipo) {
      case "DEMO":
        return createAdapterDemo(adapterNode);
      case "METAMAPA":
        return createAdapterMetaMapa(adapterNode);
      default:
        throw new IllegalArgumentException("Tipo de adaptador no soportado: " + tipo);
    }
  }

  /**
   * Crea una instancia de AdapterDemo a partir de su configuración.
   *
   * @param adapterNode El nodo JSON específico para AdapterDemo.
   * @return una nueva instancia de AdapterDemo.
   */
  private AdapterDemo createAdapterDemo(JsonNode adapterNode) {
    try {
      URL url = new URL(adapterNode.path("url")
                                   .asText());
      Conexion conexion = new Conexion();
      return new AdapterDemo(conexion, url);
    } catch (MalformedURLException e) {
      throw new RuntimeException("URL mal formada en la configuración del AdapterDemo", e);
    }
  }

  /**
   * Crea una instancia de AdapterMetaMapa a partir de su configuración.
   *
   * @param adapterNode El nodo JSON específico para AdapterMetaMapa.
   * @return una nueva instancia de AdapterMetaMapa.
   */
  private AdapterMetaMapa createAdapterMetaMapa(JsonNode adapterNode) {
    String url = adapterNode.path("url")
                            .asText();
    if (url.isEmpty()) {
      throw new IllegalArgumentException("La URL no puede estar vacía para el adaptador MetaMapa.");
    }
    ServicioMetaMapa servicio = new ServicioMetaMapa(url);

    JsonNode queryNode = adapterNode.path("query");
    ObjectMapper mapper = new ObjectMapper();
    HechoQuerys query = mapper.convertValue(queryNode, HechoQuerys.class);

    return new AdapterMetaMapa(servicio, query);
  }
}

