package ar.edu.utn.frba.dds.domain.geolocalizacion;

import ar.edu.utn.frba.dds.domain.geolocalizacion.model.GeoRefModels.*;
import ar.edu.utn.frba.dds.domain.geolocalizacion.retrofit.GeoRateLimitInterceptor;
import ar.edu.utn.frba.dds.domain.geolocalizacion.services.GeoRefService;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class GeoGeoref implements GeoApi {

  private final GeoRefService service;
  private static final String GEOREF_BASE_URL = "https://apis.datos.gob.ar/georef/api/";

  /**
   * Constructor principal para inyección de dependencias (usado en tests).
   * @param service La implementación de GeoRefService a utilizar.
   */
  public GeoGeoref(GeoRefService service) {
    this.service = service;
  }

  /**
   * Constructor para producción.
   * Crea el service standard mediante "encadenamiento de constructores" (constructor chaining).
   */
  public GeoGeoref() {
    // Llama al constructor que tiene el service (El que esta arriba) con el service que crea en createservice
    this(createService(50)); // Intervalo por defecto de 50ms.
  }

  /**
   * Fábrica privada estática que construye el GeoRefService.
   * La lógica de construcción ahora vive dentro de esta clase.
   */
  private static GeoRefService createService(long rateLimitMillis) {
    OkHttpClient client = new OkHttpClient.Builder()
        .addInterceptor(new GeoRateLimitInterceptor(rateLimitMillis))
        .readTimeout(30, TimeUnit.SECONDS)
        .connectTimeout(30, TimeUnit.SECONDS)
        .build();

    // Llama al método de utilidad estático en la interfaz GeoApi.
    Retrofit retrofit = GeoApi.buildRetrofit(client, GEOREF_BASE_URL);
    return retrofit.create(GeoRefService.class);
  }

  @Override
  public CompletableFuture<String> obtenerProvincia(double lat, double lon) {
    return service.obtenerProvincia(lat, lon, "provincia.nombre")
                  .thenApply(response -> {
                    // La ruta de acceso correcta al nombre de la provincia.
                    if (response != null && response.ubicacion != null && response.ubicacion.provinciaInfo != null) {
                      return response.ubicacion.provinciaInfo.nombre;
                    }
                    return null;
                  });
  }

  @Override
  public CompletableFuture<PuntoGeografico> obtenerUbicacion(String nombreProvincia) {
    return service.obtenerUbicacion(nombreProvincia)
                  .thenApply(response -> {
                    if (response != null && response.provincias != null && !response.provincias.isEmpty()) {
                      // El import de 'Centroide' ahora funciona correctamente.
                      Centroide centroide = response.provincias.get(0).centroide;
                      return new PuntoGeografico(centroide.lat, centroide.lon);
                    }
                    return null;
                  });
  }
}

