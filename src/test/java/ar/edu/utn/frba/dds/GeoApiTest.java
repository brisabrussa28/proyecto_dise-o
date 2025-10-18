package ar.edu.utn.frba.dds;

import ar.edu.utn.frba.dds.domain.geolocalizacion.GeoApi;
import ar.edu.utn.frba.dds.domain.geolocalizacion.GeoGeoref;
import ar.edu.utn.frba.dds.domain.geolocalizacion.GeoOpenStreetMaps;
import ar.edu.utn.frba.dds.domain.geolocalizacion.services.GeoRefService;
import ar.edu.utn.frba.dds.domain.geolocalizacion.services.OpenStreetMapsService;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.*;
import retrofit2.Retrofit;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests for GeoApi Implementations")
class GeoApiTest {

  // El servidor se inicializará antes de CADA test para asegurar el aislamiento.
  private MockWebServer mockWebServer;

  @BeforeEach
  void startServer() throws IOException {
    mockWebServer = new MockWebServer();
    mockWebServer.start();
  }

  @AfterEach
  void shutDownServer() throws IOException {
    mockWebServer.shutdown();
  }

  @Nested
  @DisplayName("Tests for GeoGeoref")
  class GeoGeorefTests {
    private GeoApi geoGeoref;

    @BeforeEach
    void setUp() {
      // 1. Creamos un cliente Retrofit que apunta a nuestro servidor de prueba.
      Retrofit retrofit = GeoApi.buildRetrofit(
          new OkHttpClient.Builder().build(),
          mockWebServer.url("/").toString()
      );

      // 2. Creamos un 'falso' service que usará este cliente Retrofit.
      GeoRefService mockService = retrofit.create(GeoRefService.class);

      // 3. Inyectamos el 'falso' service en nuestra clase.
      geoGeoref = new GeoGeoref(mockService);
    }

    @Test
    @DisplayName("Obtener provincia retorna el nombre correcto para coordenadas válidas")
    void obtenerProvinciaExitoso() throws ExecutionException, InterruptedException {
      MockResponse mockResponse = new MockResponse()
          .setResponseCode(200)
          .setHeader("Content-Type", "application/json")
          .setBody("{\"ubicacion\": {\"provincia\": {\"nombre\": \"Ciudad Autónoma de Buenos Aires\"}}}");
      mockWebServer.enqueue(mockResponse);

      CompletableFuture<String> futuro = geoGeoref.obtenerProvincia(-34.60, -58.38);
      String nombreProvincia = futuro.get();

      assertEquals("Ciudad Autónoma de Buenos Aires", nombreProvincia);
      assertEquals("/ubicacion?lat=-34.6&lon=-58.38&campos=provincia.nombre", mockWebServer.takeRequest().getPath());
    }

    @Test
    @DisplayName("Obtener ubicación retorna el punto geográfico correcto para una provincia")
    void obtenerUbicacionExitoso() throws ExecutionException, InterruptedException {
      MockResponse mockResponse = new MockResponse()
          .setResponseCode(200)
          .setHeader("Content-Type", "application/json")
          .setBody("{\"provincias\": [{\"centroide\": {\"lat\": -26.87, \"lon\": -65.36}, \"nombre\": \"Tucumán\"}]}");
      mockWebServer.enqueue(mockResponse);

      CompletableFuture<PuntoGeografico> futuro = geoGeoref.obtenerUbicacion("Tucuman");
      PuntoGeografico punto = futuro.get();

      assertNotNull(punto);
      assertEquals(-26.87, punto.getLatitud());
      assertEquals(-65.36, punto.getLongitud());
      assertEquals("/provincias?nombre=Tucuman", mockWebServer.takeRequest().getPath());
    }

    @Test
    @DisplayName("Obtener ubicación devuelve null si la provincia no existe")
    void obtenerUbicacionProvinciaInexistente() throws ExecutionException, InterruptedException {
      MockResponse mockResponse = new MockResponse()
          .setResponseCode(200)
          .setHeader("Content-Type", "application/json")
          .setBody("{\"provincias\": []}");
      mockWebServer.enqueue(mockResponse);

      CompletableFuture<PuntoGeografico> futuro = geoGeoref.obtenerUbicacion("ProvinciaInventada");
      PuntoGeografico punto = futuro.get();

      assertNull(punto);
    }
  }

  @Nested
  @DisplayName("Tests for GeoOpenStreetMaps")
  class GeoOpenStreetMapsTests {
    private GeoApi geoOpenStreetMaps;
    private static final String USER_AGENT = "MiAppDeTest/1.0";

    @BeforeEach
    void setUp() {
      // 1. Creamos un cliente Retrofit que apunta a nuestro servidor de prueba.
      Retrofit retrofit = GeoApi.buildRetrofit(
          new OkHttpClient.Builder().build(),
          mockWebServer.url("/").toString()
      );
      // 2. Creamos un 'falso' service.
      OpenStreetMapsService mockService = retrofit.create(OpenStreetMapsService.class);
      // 3. Inyectamos el 'falso' service.
      geoOpenStreetMaps = new GeoOpenStreetMaps(mockService);
    }

    @Test
    @DisplayName("Obtener provincia retorna 'state' de la API")
    void obtenerProvinciaExitoso() throws ExecutionException, InterruptedException {
      MockResponse mockResponse = new MockResponse()
          .setResponseCode(200)
          .setHeader("Content-Type", "application/json")
          .setBody("{\"address\": {\"city\": \"Rosario\", \"state\": \"Santa Fe\", \"country\": \"Argentina\"}}");
      mockWebServer.enqueue(mockResponse);

      CompletableFuture<String> futuro = geoOpenStreetMaps.obtenerProvincia(-32.95, -60.64);
      String provincia = futuro.get();

      assertEquals("Santa Fe", provincia);
      RecordedRequest request = mockWebServer.takeRequest();
      assertEquals("/reverse?format=json&lat=-32.95&lon=-60.64", request.getPath());
      // Nota: El User-Agent se añade a través de un interceptor en el código de producción.
      // En este test unitario, como inyectamos un service "desnudo", no se añade. Lo cual es correcto.
    }

    @Test
    @DisplayName("Obtener ubicación retorna lat y lon del primer resultado")
    void obtenerUbicacionExitoso() throws ExecutionException, InterruptedException {
      MockResponse mockResponse = new MockResponse()
          .setResponseCode(200)
          .setHeader("Content-Type", "application/json")
          .setBody("[{\"lat\": \"-32.8897233\", \"lon\": \"-68.8457782\"}]");
      mockWebServer.enqueue(mockResponse);

      CompletableFuture<PuntoGeografico> futuro = geoOpenStreetMaps.obtenerUbicacion("Mendoza");
      PuntoGeografico punto = futuro.get();

      assertNotNull(punto);
      assertEquals(-32.8897233, punto.getLatitud());
      assertEquals(-68.8457782, punto.getLongitud());
    }

    @Test
    @DisplayName("Obtener ubicación devuelve null si no hay resultados")
    void obtenerUbicacionSinResultados() throws ExecutionException, InterruptedException {
      MockResponse mockResponse = new MockResponse()
          .setResponseCode(200)
          .setHeader("Content-Type", "application/json")
          .setBody("[]");
      mockWebServer.enqueue(mockResponse);

      CompletableFuture<PuntoGeografico> futuro = geoOpenStreetMaps.obtenerUbicacion("LugarInexistente");
      PuntoGeografico punto = futuro.get();

      assertNull(punto);
    }
  }
}

