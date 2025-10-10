package ar.edu.utn.frba.dds.domain.geolocalizacion;

import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import java.util.concurrent.CompletableFuture;

/**
 * Interfaz que define los métodos que debe implementar cualquier servicio
 * de geolocalización para ser utilizado en el sistema.
 */
public interface GeoApi {

  /**
   * Obtiene de forma asíncrona el nombre de la provincia para un punto geográfico.
   *
   * @param lat La latitud del punto.
   * @param lon La longitud del punto.
   * @return Un CompletableFuture que se resolverá con el nombre de la provincia.
   */
  CompletableFuture<String> obtenerProvincia(double lat, double lon);

  /**
   * Obtiene de forma asíncrona las coordenadas de una provincia a partir de su nombre.
   *
   * @param nombreProvincia El nombre de la provincia a buscar.
   * @return Un CompletableFuture que se resolverá con el PuntoGeografico.
   */
  CompletableFuture<PuntoGeografico> obtenerUbicacion(String nombreProvincia);
}
