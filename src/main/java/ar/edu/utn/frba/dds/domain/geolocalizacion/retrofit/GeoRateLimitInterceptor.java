package ar.edu.utn.frba.dds.domain.geolocalizacion.retrofit;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Interceptor de OkHttp que gestiona los límites de tasa de dos maneras:
 * 1. Proactiva: Asegura un intervalo mínimo entre peticiones.
 * 2. Reactiva: Si recibe un error 429 (Too Many Requests), reintenta la petición
 * después de esperar el tiempo indicado en la cabecera 'Retry-After'.
 */
public class GeoRateLimitInterceptor implements Interceptor {
  private final long minIntervalMillis;
  private long nextAvailableTime = 0;
  private final Object lock = new Object();

  // Límite de reintentos para no entrar en bucles infinitos.
  private static final int MAX_RETRIES = 3;

  public GeoRateLimitInterceptor(long minIntervalMillis) {
    this.minIntervalMillis = minIntervalMillis;
  }

  @Override
  public Response intercept(Chain chain) throws IOException {
    // --- Parte Proactiva ---
    synchronized (lock) {
      long now = System.currentTimeMillis();
      long waitTime = (nextAvailableTime > now) ? nextAvailableTime - now : 0;
      if (waitTime > 0) {
        try {
          Thread.sleep(waitTime);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new IOException("Thread interrupted during rate limit wait", e);
        }
      }
      nextAvailableTime = System.currentTimeMillis() + minIntervalMillis;
    }

    // --- Parte Reactiva ---
    Request request = chain.request();
    Response response = chain.proceed(request);
    int retryCount = 0;

    while (!response.isSuccessful() && response.code() == 429 && retryCount < MAX_RETRIES) {
      // Cerramos la respuesta fallida anterior para liberar recursos.
      response.close();
      retryCount++;

      // Leemos la cabecera 'Retry-After' para saber cuánto esperar.
      String retryAfterHeader = response.header("Retry-After");
      long waitTimeSeconds = 20; // Un valor por defecto si la cabecera no existe.
      if (retryAfterHeader != null) {
        try {
          waitTimeSeconds = Long.parseLong(retryAfterHeader);
        } catch (NumberFormatException e) {
          // La cabecera no era un número, usamos el valor por defecto.
        }
      }

      try {
        // Esperamos el tiempo indicado por la API.
        TimeUnit.SECONDS.sleep(waitTimeSeconds);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new IOException("Thread interrupted during retry wait", e);
      }

      // Reintentamos la petición.
      response = chain.proceed(request);
    }

    return response;
  }
}

