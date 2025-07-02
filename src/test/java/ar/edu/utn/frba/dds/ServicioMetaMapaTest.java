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

import ar.edu.utn.frba.dds.domain.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.hecho.HechoQuerys;
import ar.edu.utn.frba.dds.domain.hecho.ListadoDeHechos;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.origen.Origen;
import ar.edu.utn.frba.dds.domain.reportes.Solicitud;
import ar.edu.utn.frba.dds.domain.serviciometamapa.ServicioMetaMapa;
import com.github.tomakehurst.wiremock.WireMockServer;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioMetaMapaTest {
  PuntoGeografico pgAux = new PuntoGeografico(33.39627891281455, 44.48695991794239);
  private final HechoQuerys filtros = new HechoQuerys(
      "desastres",
      pgAux,
      LocalDateTime.now()
                   .plusDays(10),
      LocalDateTime.now()
                   .plusDays(10),
      LocalDateTime.now()
                   .minusDays(10),
      LocalDateTime.now()
                   .minusDays(10)
  );
  private WireMockServer wireMockServer;
  private ServicioMetaMapa servicioMetaMapa;

  @BeforeEach
  public void setUp() {
    wireMockServer = new WireMockServer(8080);
    wireMockServer.start();
    configureFor("localhost", 8080);

    this.servicioMetaMapa = new ServicioMetaMapa("http://localhost:8080");
  }

  @AfterEach
  public void tearDown() {
    wireMockServer.stop();
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
            null
        )
    );
  }

  @Test
  public void obtenerHechos() throws IOException {
    String body = "{ \"hechos\": [ { \"id\": \"b6c5f3e1-77a0-4c4a-b922-2a4b0f4f89b1\", \"descripcion\": \"Simulado\" } ] }";

    stubFor(get(urlPathEqualTo("/hechos")).withQueryParam("categoria", equalTo("desastres"))
                                          .willReturn(aResponse().withStatus(200)
                                                                 .withHeader("Content-Type", "application/json")
                                                                 .withBody(body)));

    ListadoDeHechos listadoDeHechos = servicioMetaMapa.listadoDeHechos(filtros);
    assertNotNull(listadoDeHechos);
    assertEquals(
        1,
        listadoDeHechos.getHechos()
                       .size()
    );
  }

  @Test
  public void obtenerHechosDeColeccionUno() throws IOException {
    String body = "{ \"hechos\": [ { \"id\": \"b6c5f3e1-77a0-4c4a-b922-2a4b0f4f89b1\", \"descripcion\": \"Colección simulada\" } ] }";

    stubFor(get(urlPathEqualTo("/colecciones/1/hechos")).withQueryParam("categoria", equalTo("desastres"))
                                                        .willReturn(aResponse().withStatus(200)
                                                                               .withHeader(
                                                                                   "Content-Type",
                                                                                   "application/json"
                                                                               )
                                                                               .withBody(body)));

    ListadoDeHechos listado = servicioMetaMapa.listadoDeHechosPorColeccion(1, filtros);
    assertNotNull(listado);
    assertEquals(
        1,
        listado.getHechos()
               .size()
    );
  }

  @Test
  public void seCreaSolicitud() throws IOException {
    stubFor(post(urlEqualTo("/solicitudes")).willReturn(aResponse().withStatus(201)
                                                                   .withHeader("Content-Type", "application/json")
                                                                   .withBody(
                                                                       "{ \"id\": \"b6c5f3e1-77a0-4c4a-b922-2a4b0f4f89b1\" }")));

    PuntoGeografico pgAux = new PuntoGeografico(33.39627891281455, 44.48695991794239);
    String motivo = "a".repeat(600);
    List<String> etiquetasAux = List.of(
        "#ancianita",
        "#robo_a_mano_armada",
        "#violencia",
        "#leyDeProtecciónALasAncianitas",
        "#NOalaVIOLENCIAcontraABUELITAS"
    );

    Hecho hecho = new Hecho(
        "titulo",
        "desc",
        "Robos",
        "direccion",
        pgAux,
        LocalDateTime.now(),
        LocalDateTime.now(),
        Origen.PROVISTO_CONTRIBUYENTE,
        etiquetasAux
    );

    FuenteDinamica fuente = new FuenteDinamica("MiFuente", null);
    fuente.agregarHecho(hecho);

    Solicitud solicitud = new Solicitud(UUID.randomUUID(), hecho, motivo);
    int codigo = servicioMetaMapa.enviarSolicitud(solicitud);
    assertEquals(201, codigo);
  }
}