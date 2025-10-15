package ar.edu.utn.frba.dds.domain.hecho;

import ar.edu.utn.frba.dds.domain.geolocalizacion.GeoApi;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Orquesta el enriquecimiento de una lista de Hechos, utilizando una GeoApi
 * para completar información geográfica faltante. Ofrece métodos tanto síncronos
 * como asíncronos para diferentes casos de uso.
 */
public class EnriquecedorDeHechos {
  private final GeoApi geoApi;

  public EnriquecedorDeHechos(GeoApi geoApi) {
    if (geoApi == null) {
      throw new IllegalArgumentException("La implementación de GeoApi no puede ser nula.");
    }
    this.geoApi = geoApi;
  }

  /**
   * Enriquece una lista de hechos de forma síncrona (bloqueante).
   * Espera a que todas las operaciones de la API terminen antes de devolver el resultado.
   * Ideal para procesos de guardado en base de datos donde se necesita el dato completo.
   *
   * @param hechos La lista de hechos a completar.
   * @return Una NUEVA lista con los hechos enriquecidos.
   */
  public List<Hecho> completar(List<Hecho> hechos) {
    // Llama al método asíncrono y bloquea el hilo actual hasta que se complete.
    return this.completarAsincrono(hechos).join();
  }

  /**
   * Inicia el enriquecimiento de una lista de hechos de forma asíncrona (no bloqueante).
   * Devuelve inmediatamente un CompletableFuture que se completará con el resultado.
   * Ideal para procesos en segundo plano o interfaces de usuario.
   *
   * @param hechos La lista de hechos a completar.
   * @return Un CompletableFuture que se resolverá con una NUEVA lista de hechos enriquecidos.
   */
  public CompletableFuture<List<Hecho>> completarAsincrono(List<Hecho> hechos) {
    if (hechos == null || hechos.isEmpty()) {
      return CompletableFuture.completedFuture(Collections.emptyList());
    }

    List<CompletableFuture<Hecho>> futuros = hechos.stream()
                                                   .map(this::completarHechoIndividual)
                                                   .collect(Collectors.toList());

    CompletableFuture<Void> allFutures = CompletableFuture.allOf(
        futuros.toArray(new CompletableFuture[0])
    );

    return allFutures.thenApply(v ->
                                    futuros.stream()
                                           .map(CompletableFuture::join) // .join() es seguro aquí porque allOf() garantiza la finalización
                                           .collect(Collectors.toList())
    );
  }

  /**
   * Lógica para completar un único hecho. Devuelve un futuro con el hecho,
   * ya sea el original o una copia nueva con los datos completados.
   */
  private CompletableFuture<Hecho> completarHechoIndividual(Hecho hechoOriginal) {
    boolean necesitaProvincia = (hechoOriginal.getProvincia() == null || hechoOriginal.getProvincia().isBlank()) && hechoOriginal.getUbicacion() != null;
    boolean necesitaUbicacion = hechoOriginal.getUbicacion() == null && hechoOriginal.getProvincia() != null && !hechoOriginal.getProvincia().isBlank();

    if (necesitaProvincia) {
      return geoApi.obtenerProvincia(hechoOriginal.getUbicacion().getLatitud(), hechoOriginal.getUbicacion().getLongitud())
                   .thenApply(provincia -> {
                     if (provincia != null && !provincia.isBlank()) {
                       HechoBuilder builder = new HechoBuilder().copiar(hechoOriginal);
                       builder.conProvincia(provincia);
                       return builder.build();
                     }
                     return hechoOriginal;
                   }).exceptionally(ex -> {
            System.err.println("Error al obtener provincia para el hecho: " + hechoOriginal.getTitulo() + " - " + ex.getMessage());
            return hechoOriginal;
          });
    }

    if (necesitaUbicacion) {
      return geoApi.obtenerUbicacion(hechoOriginal.getProvincia())
                   .thenApply(ubicacion -> {
                     if (ubicacion != null) {
                       HechoBuilder builder = new HechoBuilder().copiar(hechoOriginal);
                       builder.conUbicacion(ubicacion);
                       return builder.build();
                     }
                     return hechoOriginal;
                   }).exceptionally(ex -> {
            System.err.println("Error al obtener ubicación para el hecho: " + hechoOriginal.getTitulo() + " - " + ex.getMessage());
            return hechoOriginal;
          });
    }

    return CompletableFuture.completedFuture(hechoOriginal);
  }
}

