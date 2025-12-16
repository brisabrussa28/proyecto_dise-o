package ar.edu.utn.frba.dds.model.lector.csv.filaconverter.model.geolocalizacion.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Contiene las clases (POJOs) que representan las respuestas JSON de la API de OpenStreetMaps (Nominatim).
 * Retrofit utiliza estas clases para deserializar automáticamente las respuestas.
 */
public class OpenStreetMapsModels {

  // --- Clase para el endpoint /search ---
  // La respuesta es una lista de estos objetos.
  public static class OpenStreetMapsSearchResponse {
    public double lat;
    public double lon;
  }


  // --- Clases para el endpoint /reverse ---
  // La respuesta es un único objeto de este tipo.
  public static class OpenStreetMapsReverseResponse {
    public Address address;
  }

  public static class Address {
    // El campo en el JSON de OSM para la provincia se llama "state".
    @JsonProperty("state")
    public String state;
  }
}

