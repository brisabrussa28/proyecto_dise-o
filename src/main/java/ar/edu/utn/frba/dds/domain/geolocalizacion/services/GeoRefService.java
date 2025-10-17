package ar.edu.utn.frba.dds.domain.geolocalizacion.services;

import ar.edu.utn.frba.dds.domain.geolocalizacion.model.GeoRefModels.GeoRefProvinciaResponse;
import ar.edu.utn.frba.dds.domain.geolocalizacion.model.GeoRefModels.GeoRefUbicacionResponse;
import retrofit2.http.GET;
import retrofit2.http.Query;
import java.util.concurrent.CompletableFuture;

/**
 * Interfaz de Retrofit para la API de GeoRef Argentina.
 */
public interface GeoRefService {

  @GET("provincias")
  CompletableFuture<GeoRefProvinciaResponse> obtenerUbicacion(
      @Query("nombre") String nombreProvincia
  );

  @GET("ubicacion")
  CompletableFuture<GeoRefUbicacionResponse> obtenerProvincia(
      @Query("lat") double lat,
      @Query("lon") double lon,
      @Query("campos") String campos
  );
}

