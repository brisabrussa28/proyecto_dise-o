package ar.edu.utn.frba.dds;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import ar.edu.utn.frba.dds.domain.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.hecho.HechoBuilder;
import ar.edu.utn.frba.dds.domain.fuentes.apis.serviciometamapa.HechoQuerys;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.origen.Origen;
import ar.edu.utn.frba.dds.domain.reportes.Solicitud;
import ar.edu.utn.frba.dds.domain.fuentes.apis.serviciometamapa.ServicioMetaMapa;
import ar.edu.utn.frba.dds.domain.serializadores.Serializador;
import com.github.tomakehurst.wiremock.WireMockServer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ServicioMetaMapaTest {
  PuntoGeografico pgAux = new PuntoGeografico(33.39627891281455, 44.48695991794239);
  private final HechoQuerys filtros = new HechoQuerys(
      "desastres",
      "Buenos Aires",
      pgAux,
      LocalDateTime.now().plusDays(10),
      LocalDateTime.now().plusDays(10),
      LocalDateTime.now().minusDays(10),
      LocalDateTime.now().minusDays(10)
  );
  private WireMockServer wireMockServer;
  private ServicioMetaMapa servicioMetaMapa;
  private Path tempJsonFile;
  @Mock
  private Serializador<Hecho> serializadorMock;

  @BeforeEach
  public void setUp() throws IOException {
    MockitoAnnotations.openMocks(this);
    wireMockServer = new WireMockServer(8080);
    wireMockServer.start();
    configureFor("localhost", 8080);

    this.servicioMetaMapa = new ServicioMetaMapa("http://localhost:8080");
    tempJsonFile = Files.createTempFile("test_servicio_metamapa_", ".json");
    when(serializadorMock.importar(anyString())).thenReturn(new ArrayList<>());
  }

  @AfterEach
  public void tearDown() throws IOException {
    wireMockServer.stop();
    Files.deleteIfExists(tempJsonFile);
  }

  @Test
  public void siHagoQueryConFechaNullLanzaExcepcion() {
    assertThrows(
        RuntimeException.class, () -> new HechoQuerys(
            null,
            null,
            null,
            null,
            null,
            null,
            null
        )
    );
  }

  @Test
  public void obtenerHechos() throws IOException {
    String body = "[ { \"titulo\": \"Hecho Simulado\", \"descripcion\": \"Simulado\", \"fechaSuceso\": \"2023-10-27T10:00:00\" } ]";

    stubFor(get(urlPathEqualTo("/hechos")).withQueryParam("categoria", equalTo("desastres"))
        .willReturn(aResponse().withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(body)));

    List<Hecho> listadoDeHechos = servicioMetaMapa.listadoDeHechos(filtros);
    assertNotNull(listadoDeHechos);
    assertEquals(1, listadoDeHechos.size());
  }

  @Test
  public void obtenerHechosDeColeccionUno() throws IOException {
    String body = "[ { \"titulo\": \"Hecho de Colección\", \"descripcion\": \"Colección simulada\", \"fechaSuceso\": \"2023-10-27T11:00:00\" } ]";

    stubFor(get(urlPathEqualTo("/colecciones/1/hechos")).withQueryParam("categoria", equalTo("desastres"))
        .willReturn(aResponse().withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(body)));

    List<Hecho> listado = servicioMetaMapa.listadoDeHechosPorColeccion(1, filtros);
    assertNotNull(listado);
    assertEquals(1, listado.size());
  }

  @Test
  public void seCreaSolicitud() throws IOException {
    stubFor(post(urlEqualTo("/solicitudes")).willReturn(aResponse().withStatus(201)
        .withHeader("Content-Type", "application/json")
        .withBody(
            "{ \"id\": \"b6c5f3e1-77a0-4c4a-b922-2a4b0f4f89b1\" }")));

    Hecho hecho = new HechoBuilder()
        .conTitulo("titulo")
        .conFechaSuceso(LocalDateTime.now().minusDays(1))
        .build();

    FuenteDinamica fuente = new FuenteDinamica("MiFuente", tempJsonFile.toString(), serializadorMock);
    fuente.agregarHecho(hecho);
    String motivo = "a".repeat(501);
    Solicitud solicitud = new Solicitud(UUID.randomUUID(), hecho, motivo);
    int codigo = servicioMetaMapa.enviarSolicitud(solicitud);
    assertEquals(201, codigo);
  }
}
