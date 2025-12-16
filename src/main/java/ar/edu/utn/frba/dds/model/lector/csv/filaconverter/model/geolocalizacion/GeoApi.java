package ar.edu.utn.frba.dds.model.lector.csv.filaconverter.model.geolocalizacion;

import ar.edu.utn.frba.dds.model.info.PuntoGeografico;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
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

  /**
   * Construye una instancia base de Retrofit.
   * Es package-private para que solo las clases de este paquete puedan usarlo.
   */
  static Retrofit buildRetrofit(OkHttpClient client, String baseUrl) {
    ObjectMapper mapper = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    return new Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(JacksonConverterFactory.create(mapper))
        .build();
  }
}
