package ar.edu.utn.frba.dds;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ar.edu.utn.frba.dds.domain.fuentes.apis.AdapterMetaMapa;
import ar.edu.utn.frba.dds.domain.fuentes.apis.serviciometamapa.HechoQuerys;
import ar.edu.utn.frba.dds.domain.fuentes.apis.serviciometamapa.ServicioMetaMapa;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.hecho.HechoBuilder;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas para el Adaptador de MetaMapa y sus componentes")
public class AdapterMetaMapaTest {

  @Mock
  private ServicioMetaMapa servicioMetaMapaMock;
  private HechoQuerys queryMock;
  private AdapterMetaMapa adapter;

  @BeforeEach
  void setUp() {
    LocalDateTime now = LocalDateTime.now();
    queryMock = new HechoQuerys("Robo", "BsAs", new PuntoGeografico(1.0, 1.0), now, now, now, now);
    adapter = new AdapterMetaMapa(servicioMetaMapaMock, queryMock);
  }

  @Test
  @DisplayName("Adapter delega la llamada de consultarHechos al servicio")
  void consultarHechosDelegaCorrectamente() throws IOException {
    List<Hecho> listaEsperada = List.of(new HechoBuilder()
                                            .conTitulo("Hecho 1")
                                            .conFechaSuceso(LocalDateTime.now().minusDays(1))
                                            .build());
    when(servicioMetaMapaMock.listadoDeHechos(queryMock)).thenReturn(listaEsperada);
    List<Hecho> resultado = adapter.consultarHechos();
    assertEquals(listaEsperada, resultado);
    verify(servicioMetaMapaMock).listadoDeHechos(queryMock);
  }


  // --- Pruebas para el Value Object HechoQuerys ---
  @Nested
  @DisplayName("Pruebas para HechoQuerys")
  class HechoQuerysTest {
    private final LocalDateTime now = LocalDateTime.now();

    @Test
    @DisplayName("La creación con fechas nulas lanza una excepción")
    void crearHechoQuerysConFechaNulaLanzaExcepcion() {
      assertThrows(RuntimeException.class, () -> new HechoQuerys(null, null, null, null, now, now, now));
    }

    @Test
    @DisplayName("Se crea correctamente con valores válidos")
    void crearHechoQuerysConValoresValidos() {
      HechoQuerys querys = new HechoQuerys("Cat", "Prov", null, now, now, now, now);
      assertEquals("Cat", querys.getCategoria());
      assertEquals("Prov", querys.getProvincia());
      assertNotNull(querys.getFechaAcontecimientoDesde());
    }
  }

  // --- Pruebas de ServicioMetaMapa con un servidor mockeado ---
  @Nested
  @DisplayName("Pruebas de integración para ServicioMetaMapa")
  class ServicioMetaMapaIntegrationTest {
    private MockWebServer mockWebServer;
    private ServicioMetaMapa servicio;
    private HechoQuerys querysDePrueba;

    @BeforeEach
    void setUp() throws IOException {
      mockWebServer = new MockWebServer();
      mockWebServer.start();
      String baseUrl = mockWebServer.url("/").toString();
      servicio = new ServicioMetaMapa(baseUrl);

      LocalDateTime now = LocalDateTime.now();
      querysDePrueba = new HechoQuerys("Incendio", "CABA", new PuntoGeografico(0, 0), now, now, now, now);
    }

    @AfterEach
    void tearDown() throws IOException {
      mockWebServer.shutdown();
    }

    @Test
    @DisplayName("Deserializa correctamente una respuesta exitosa")
    void listadoDeHechosDeserializaRespuestaExitosa() throws IOException {
      String jsonResponse = "[{\"hecho_titulo\":\"Incendio en Almagro\"}]";
      mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(jsonResponse).setHeader("Content-Type", "application/json"));
      List<Hecho> hechos = servicio.listadoDeHechos(querysDePrueba);
      assertEquals(1, hechos.size());
      assertEquals("Incendio en Almagro", hechos.get(0).getTitulo());
    }

    @Test
    @DisplayName("Devuelve lista vacía si el body de la respuesta es un array vacío")
    void listadoDeHechosConBodyNulo() throws IOException {
      String jsonResponse = "[]";
      mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(jsonResponse).setHeader("Content-Type", "application/json"));
      List<Hecho> hechos = servicio.listadoDeHechos(querysDePrueba);
      assertTrue(hechos.isEmpty());
    }

    @Test
    @DisplayName("Consulta por colección funciona correctamente")
    void listadoDeHechosPorColeccion() throws IOException {
      String jsonResponse = "[{\"hecho_titulo\":\"Corte de Luz\"}]";
      mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(jsonResponse));
      List<Hecho> hechos = servicio.listadoDeHechosPorColeccion(123, querysDePrueba);
      assertEquals(1, hechos.size());
      System.out.println(hechos.get(0).getTitulo());
      assertEquals("Corte de Luz", hechos.get(0).getTitulo());
    }

    @Test
    @DisplayName("Una respuesta de error 500 del servidor devuelve una lista vacía")
    void errorDelServidorLanzaExcepcion() throws IOException {
      mockWebServer.enqueue(new MockResponse().setResponseCode(500));
      List<Hecho> hechos = servicio.listadoDeHechos(querysDePrueba);
      assertTrue(hechos.isEmpty());
    }
  }
}

