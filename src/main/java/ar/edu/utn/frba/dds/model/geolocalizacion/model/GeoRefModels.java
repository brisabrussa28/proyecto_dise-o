package ar.edu.utn.frba.dds.model.geolocalizacion.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Contiene las clases (POJOs) que representan las respuestas JSON de la API de GeoRef.
 * Retrofit utiliza estas clases para deserializar automáticamente las respuestas.
 */
public class GeoRefModels {

  // --- Clases para el endpoint /provincias ---

  public static class GeoRefProvinciaResponse {
    public List<Provincia> provincias;
  }

  public static class Provincia {
    public Centroide centroide;
  }

  public static class Centroide {
    public double lat;
    public double lon;
  }


  // --- Clases para el endpoint /ubicacion ---

  public static class GeoRefUbicacionResponse {
    public Ubicacion ubicacion;
  }

  public static class Ubicacion {
    @JsonProperty("provincia") // Mapea el campo 'provincia' del JSON a esta propiedad
    public ProvinciaInfo provinciaInfo;
  }

  public static class ProvinciaInfo {
    public String nombre;
  }
}

