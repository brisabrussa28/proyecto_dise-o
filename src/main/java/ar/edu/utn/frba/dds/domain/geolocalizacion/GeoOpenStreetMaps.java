package ar.edu.utn.frba.dds.domain.geolocalizacion;

import ar.edu.utn.frba.dds.domain.geolocalizacion.model.OpenStreetMapsModels.*;
import ar.edu.utn.frba.dds.domain.geolocalizacion.retrofit.GeoRateLimitInterceptor;
import ar.edu.utn.frba.dds.domain.geolocalizacion.retrofit.GeoUserAgentInterceptor;
import ar.edu.utn.frba.dds.domain.geolocalizacion.services.OpenStreetMapsService;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class GeoOpenStreetMaps implements GeoApi {

  private final OpenStreetMapsService service;
  private static final String OSM_BASE_URL = "https://nominatim.openstreetmap.org/";

  /**
   * Constructor principal para inyección de dependencias (usado en tests).
   * @param service La implementación de OpenStreetMapsService a utilizar.
   */
  public GeoOpenStreetMaps(OpenStreetMapsService service) {
    this.service = service;
  }

  /**
   * Constructor para producción. Crea su propia dependencia.
   * @param userAgent El User-Agent es requerido por la API de OpenStreetMaps.
   */
  public GeoOpenStreetMaps(String userAgent) {
    // Llama al constructor principal, pasándole un servicio creado por su propia fábrica.
    this(createService(userAgent, 1000)); // OSM requiere un intervalo de 1000ms.
  }

  /**
   * Fábrica privada estática que construye el OpenStreetMapsService.
   * La lógica de construcción ahora vive dentro de esta clase.
   */
  private static OpenStreetMapsService createService(String userAgent, long rateLimitMillis) {
    if (userAgent == null || userAgent.isBlank()) {
      throw new IllegalArgumentException("El User-Agent no puede estar vacío.");
    }

    OkHttpClient client = new OkHttpClient.Builder()
        .addInterceptor(new GeoRateLimitInterceptor(rateLimitMillis))
        .addInterceptor(new GeoUserAgentInterceptor(userAgent)) // Interceptor específico para OSM
        .readTimeout(30, TimeUnit.SECONDS)
        .connectTimeout(30, TimeUnit.SECONDS)
        .build();

    // Llama al método de utilidad estático en la interfaz GeoApi.
    Retrofit retrofit = GeoApi.buildRetrofit(client, OSM_BASE_URL);
    return retrofit.create(OpenStreetMapsService.class);
  }

  @Override
  public CompletableFuture<String> obtenerProvincia(double lat, double lon) {
    return service.obtenerProvincia(lat, lon)
                  .thenApply(response -> {
                    if (response != null && response.address != null) {
                      return response.address.state;
                    }
                    return null;
                  });
  }

  @Override
  public CompletableFuture<PuntoGeografico> obtenerUbicacion(String nombreProvincia) {
    return service.obtenerUbicacion(nombreProvincia)
                  .thenApply(response -> {
                    if (response != null && !response.isEmpty()) {
                      OpenStreetMapsSearchResponse primerResultado = response.get(0);
                      return new PuntoGeografico(primerResultado.lat, primerResultado.lon);
                    }
                    return null;
                  });
  }
}

