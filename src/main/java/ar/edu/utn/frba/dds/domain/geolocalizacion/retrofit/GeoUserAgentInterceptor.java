package ar.edu.utn.frba.dds.domain.geolocalizacion.retrofit;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;

/**
 * Interceptor de OkHttp para añadir un User-Agent a todas las peticiones de geolocalización.
 * Requerido por la API de OpenStreetMaps.
 */
public class GeoUserAgentInterceptor implements Interceptor {
  private final String userAgent;

  public GeoUserAgentInterceptor(String userAgent) {
    this.userAgent = userAgent;
  }

  @Override
  public Response intercept(Chain chain) throws IOException {
    Request originalRequest = chain.request();
    Request requestWithUserAgent = originalRequest.newBuilder()
                                                  .header("User-Agent", userAgent)
                                                  .build();
    return chain.proceed(requestWithUserAgent);
  }
}
