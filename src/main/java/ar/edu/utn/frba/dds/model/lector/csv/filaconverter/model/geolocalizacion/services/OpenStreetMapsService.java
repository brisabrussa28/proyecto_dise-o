package ar.edu.utn.frba.dds.model.lector.csv.filaconverter.model.geolocalizacion.services;

import ar.edu.utn.frba.dds.model.geolocalizacion.model.OpenStreetMapsModels.OpenStreetMapsReverseResponse;
import ar.edu.utn.frba.dds.model.geolocalizacion.model.OpenStreetMapsModels.OpenStreetMapsSearchResponse;
import retrofit2.http.GET;
import retrofit2.http.Query;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Interfaz de Retrofit para la API de OpenStreetMaps (Nominatim).
 */
public interface OpenStreetMapsService {

  @GET("search?format=json&countrycodes=ar")
  CompletableFuture<List<OpenStreetMapsSearchResponse>> obtenerUbicacion(
      @Query("q") String nombreProvincia
  );

  @GET("reverse?format=json")
  CompletableFuture<OpenStreetMapsReverseResponse> obtenerProvincia(
      @Query("lat") double lat,
      @Query("lon") double lon
  );
}

