package ar.edu.utn.frba.dds.domain.geolocalizacion;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Gestiona el espaciado de las peticiones a una API para cumplir con los límites de tasa.
 * Esta clase es thread-safe.
 */
public class LimitadorRequests {

  private final long minIntervalMillis;
  private long nextAvailableTime = 0;
  private final Object lock = new Object();

  /**
   * Crea un limitador con un intervalo mínimo entre peticiones.
   * @param minIntervalMillis El tiempo mínimo en milisegundos entre el inicio de cada petición.
   */
  public LimitadorRequests(long minIntervalMillis) {
    this.minIntervalMillis = minIntervalMillis;
  }

  /**
   * Envía una tarea para ser ejecutada, respetando el intervalo mínimo.
   * @param taskSupplier Un proveedor que devuelve el CompletableFuture de la tarea a ejecutar.
   * @return Un CompletableFuture que se completará con el resultado de la tarea.
   */
  public <T> CompletableFuture<T> submit(Supplier<CompletableFuture<T>> taskSupplier) {
    long delay;
    synchronized (lock) {
      long now = System.currentTimeMillis();
      delay = (nextAvailableTime > now) ? nextAvailableTime - now : 0;
      nextAvailableTime = now + delay + minIntervalMillis;
    }

    // Usamos un delayedExecutor para programar la ejecución de la tarea después del retraso necesario
    // sin bloquear el hilo actual.
    CompletableFuture<T> resultFuture = new CompletableFuture<>();
    CompletableFuture.delayedExecutor(delay, TimeUnit.MILLISECONDS).execute(() -> {
      // Una vez que el retraso ha pasado, ejecutamos la tarea real
      taskSupplier.get().whenComplete((result, throwable) -> {
        if (throwable != null) {
          resultFuture.completeExceptionally(throwable);
        } else {
          resultFuture.complete(result);
        }
      });
    });

    return resultFuture;
  }
}
