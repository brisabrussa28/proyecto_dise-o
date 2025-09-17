package ar.edu.utn.frba.dds;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import ar.edu.utn.frba.dds.domain.filtro.Filtro;
import ar.edu.utn.frba.dds.domain.filtro.condiciones.CondicionFactory;
import ar.edu.utn.frba.dds.domain.filtro.condiciones.condicion.Condicion;
import ar.edu.utn.frba.dds.domain.filtro.condiciones.condicion.CondicionAnd;
import ar.edu.utn.frba.dds.domain.filtro.condiciones.condicion.CondicionGenerica;
import ar.edu.utn.frba.dds.domain.filtro.condiciones.condicion.CondicionNot;
import ar.edu.utn.frba.dds.domain.filtro.condiciones.condicion.CondicionOr;
import ar.edu.utn.frba.dds.domain.filtro.condiciones.condicion.CondicionPredicado;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.hecho.HechoBuilder;
import ar.edu.utn.frba.dds.domain.hecho.Origen;
import ar.edu.utn.frba.dds.domain.hecho.etiqueta.Etiqueta;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.serializadores.exportador.Exportador;
import ar.edu.utn.frba.dds.domain.serializadores.lector.Lector;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class FiltroTest {
  PuntoGeografico pgAux = new PuntoGeografico(33.39627891281455, 44.48695991794239);
  FuenteDinamica fuenteAuxD;
  LocalDateTime horaAux = LocalDateTime.now()
                                       .minusDays(1);
  Hecho hechoParaPruebas = new Hecho(
      "Prueba",
      "Prueba",
      "Robos",
      "123",
      "PBA",
      new PuntoGeografico(123, 123),
      LocalDateTime.now(),
      LocalDateTime.now(),
      Origen.PROVISTO_CONTRIBUYENTE,
      List.of(new Etiqueta("#ROBO"))
  );
  Hecho otroHechoParaPruebas = new Hecho(
      "Prueba",
      "Prueba",
      "Pruebas",
      "direccion",
      "PBA",
      new PuntoGeografico(123, 123),
      LocalDateTime.now(),
      LocalDateTime.now(),
      Origen.PROVISTO_CONTRIBUYENTE,
      List.of(new Etiqueta("#ROBO"))
  );
  @Mock
  private Lector<Hecho> lectorMock;
  @Mock
  private Exportador<Hecho> exportadorMock;
  private Path tempJsonFile;
  private Hecho hechoDePrueba;

  @BeforeEach
  void setUp() throws IOException {
    MockitoAnnotations.openMocks(this);
    tempJsonFile = Files.createTempFile("test_fuente_dinamica_", ".json");
    when(lectorMock.importar(anyString())).thenReturn(new ArrayList<>());
    fuenteAuxD = new FuenteDinamica(
        "Julio Cesar",
        tempJsonFile.toString(),
        lectorMock,
        exportadorMock
    );

    hechoDePrueba = new HechoBuilder()
        .conTitulo("titulo")
        .conDescripcion("Un día más siendo del conurbano")
        .conCategoria("Robos")
        .conDireccion("dire")
        .conProvincia("Buenos Aires")
        .conUbicacion(pgAux)
        .conFechaSuceso(horaAux)
        .conFechaCarga(LocalDateTime.now())
        .conFuenteOrigen(Origen.PROVISTO_CONTRIBUYENTE)
        .build();
  }

  @AfterEach
  void tearDown() throws IOException {
    Files.deleteIfExists(tempJsonFile);
  }

  @Test
  public void filtraPorCategoriaCorrectamente() {
    List<Hecho> hechos = List.of(hechoDePrueba);
    Condicion condicionCategoria = new CondicionGenerica("categoria", "IGUAL", "Robos");
    Filtro filtro = new Filtro(condicionCategoria);

    List<Hecho> resultado = filtro.filtrar(hechos);
    assertEquals(1, resultado.size());
    assertEquals(
        "Robos",
        resultado.get(0)
                 .getCategoria()
    );
  }

  @Test
  public void filtraPorDireccionCorrectamente() {
    List<Hecho> hechos = List.of(hechoDePrueba);
    Condicion condicionDireccion = new CondicionGenerica("direccion", "IGUAL", "dire");
    Filtro filtro = new Filtro(condicionDireccion);

    List<Hecho> resultado = filtro.filtrar(hechos);
    assertEquals(1, resultado.size());
    assertEquals(
        "dire",
        resultado.get(0)
                 .getDireccion()
    );
  }

  @Test
  public void filtraPorFechaSucesoCorrectamente() {
    List<Hecho> hechos = List.of(hechoDePrueba);
    Condicion condicionFecha = new CondicionGenerica("fechasuceso", "IGUAL", horaAux);
    Filtro filtro = new Filtro(condicionFecha);
    assertEquals(
        1,
        filtro.filtrar(hechos)
              .size()
    );
  }


  @Test
  public void aplicaVariosFiltrosCorrectamente() {
    List<Hecho> hechos = List.of(hechoDePrueba);

    CondicionAnd condicionAnd = new CondicionAnd();
    condicionAnd.agregarCondicion(new CondicionGenerica("categoria", "IGUAL", "Robos"));
    condicionAnd.agregarCondicion(new CondicionGenerica("direccion", "IGUAL", "dire"));

    Filtro filtro = new Filtro(condicionAnd);
    assertEquals(
        1,
        filtro.filtrar(hechos)
              .size()
    );
  }

  @Test
  public void aplicaVariosFiltrosYFalla() {
    List<Hecho> hechos = List.of(hechoDePrueba);

    CondicionAnd condicionAnd = new CondicionAnd();
    condicionAnd.agregarCondicion(new CondicionGenerica("categoria", "IGUAL", "Robos"));
    condicionAnd.agregarCondicion(new CondicionGenerica(
        "direccion",
        "IGUAL",
        "direccion_incorrecta"
    ));

    Filtro filtro = new Filtro(condicionAnd);
    assertTrue(filtro.filtrar(hechos)
                     .isEmpty());
  }

  @DisplayName("Pruebas para CondicionFactory")
  @Test
  void conUnJsonCreoUnaCondicionParaFiltrar() {
    var logger = Logger.getLogger(FiltroTest.class.getName());
    var condicionFactory = new CondicionFactory();
    var jsonString = """
        {
          "campo": "categoria",
          "operador": "IGUAL",
          "valor": "Robos"
        }""";
    var condicion = condicionFactory.crearCondicionDesdeJson(jsonString);
    var hechos = List.of(hechoParaPruebas, otroHechoParaPruebas);
    var filtro = new Filtro(condicion);
    logger.info("Ahora deberia filtrar por la condicion que se armo en el json, en este caso por categoria 'Robos'");
    var hechosFiltrados = filtro.filtrar(hechos);
    assertEquals(1, hechosFiltrados.size());
    assertTrue(hechosFiltrados.stream()
                              .allMatch(hecho -> hecho.getCategoria()
                                                      .equals("Robos")));
  }

  @Test
  void creoUnaCondicionParaFiltroCompuesto() {
    var condicionFactory = new CondicionFactory();
    var jsonString = """
        {
          "compuesta": "OR",
          "condiciones": [
            {
              "campo": "categoria",
              "operador": "IGUAL",
              "valor": "Robos"
            },
            {
              "campo": "direccion",
              "operador": "IGUAL",
              "valor": "direccion"
            }
          ]
        }
        """;
    var condicion = condicionFactory.crearCondicionDesdeJson(jsonString);
    var hechos = List.of(hechoParaPruebas, otroHechoParaPruebas);
    var filtro = new Filtro(condicion);
    var hechosFiltrados = filtro.filtrar(hechos);
    assertEquals(2, hechosFiltrados.size());
    assertTrue(hechosFiltrados.stream()
                              .allMatch(h -> h.getCategoria()
                                              .equals("Robos") || h.getDireccion()
                                                                   .equals("direccion")));
  }

  @Test
  void creoUnaCondicionParaFiltroDeCondicionCompuestaConAND() {
    var condicionFactory = new CondicionFactory();
    var jsonString = """
        {
          "compuesta": "AND",
          "condiciones":[
            {
              "campo": "categoria",
              "operador": "IGUAL",
              "valor": "Robos"
            },
            {
              "campo": "direccion",
              "operador": "IGUAL",
              "valor": "direc"
            }
          ]
        }
        """;
    var condicion = condicionFactory.crearCondicionDesdeJson(jsonString);
    var hechos = List.of(hechoParaPruebas, otroHechoParaPruebas);
    var filtro = new Filtro(condicion);
    var hechosFiltrados = filtro.filtrar(hechos);
    assertEquals(0, hechosFiltrados.size());
    assertTrue(hechosFiltrados.stream()
                              .allMatch(h -> h.getCategoria()
                                              .equals("Robos")));
  }

  @Test
  void filtroPorLosQueNOCumplen() {
    var condicionFactory = new CondicionFactory();
    var jsonString = """
        {
          "logica": "NOT",
          "condicion":
            {
              "campo": "categoria",
              "operador": "IGUAL",
              "valor": "Robos"
            }
        }
        """;
    var condicion = condicionFactory.crearCondicionDesdeJson(jsonString);
    var hechos = List.of(hechoParaPruebas, otroHechoParaPruebas);
    var filtro = new Filtro(condicion);
    var hechosFiltrados = filtro.filtrar(hechos);
    assertEquals(1, hechosFiltrados.size());
  }

  @Test
  void testCondicionNot() {
    var hechos = List.of(hechoParaPruebas, otroHechoParaPruebas);

    var condicionInterna = new CondicionGenerica("categoria", "IGUAL", "Robos");

    var condicionNot = new CondicionNot();
    condicionNot.setCondicion(condicionInterna);

    var filtro = new Filtro(condicionNot);
    var resultados = filtro.filtrar(hechos);

    assertEquals(1, resultados.size());
    assertEquals(
        "Pruebas",
        resultados.get(0)
                  .getCategoria()
    );
  }

  @Test
  void testCondicionOrConMultiplesConditiones() {
    var hechos = List.of(hechoParaPruebas, otroHechoParaPruebas);

    var condicionOr = new CondicionOr();
    condicionOr.agregarCondicion(new CondicionGenerica("categoria", "IGUAL", "Categoria_Inexistente"));
    condicionOr.agregarCondicion(new CondicionGenerica("direccion", "IGUAL", "direccion"));

    var filtro = new Filtro(condicionOr);
    var resultados = filtro.filtrar(hechos);

    assertEquals(1, resultados.size());
    assertEquals(
        "direccion",
        resultados.get(0)
                  .getDireccion()
    );
  }

  @Test
  void testCondicionCompuestaConAnd() {
    var hechos = List.of(hechoParaPruebas, otroHechoParaPruebas);
    var condicionAnd = new CondicionAnd();
    condicionAnd.agregarCondicion(new CondicionGenerica("provincia", "IGUAL", "PBA"));
    condicionAnd.agregarCondicion(new CondicionGenerica("categoria", "IGUAL", "Robos"));

    var filtro = new Filtro(condicionAnd);
    var hechosFiltrados = filtro.filtrar(hechos);

    assertEquals(1, hechosFiltrados.size());
    Assertions.assertIterableEquals(
        List.of("PBA"),
        hechosFiltrados.stream()
                       .map(h -> h.getProvincia())
                       .toList()
    );
  }

  @Test
  void noSeQuePonerEnPredicado() {
    var hechos = List.of(hechoParaPruebas, otroHechoParaPruebas);
    var condicionPredicado = new CondicionPredicado(h -> h.getTitulo()
                                                          .equals("Prueba"));
    var filtro = new Filtro(condicionPredicado);
    var hechosFiltrados = filtro.filtrar(hechos);
    assertEquals(2, hechosFiltrados.size());
    Assertions.assertIterableEquals(
        List.of("Prueba", "Prueba"),
        hechosFiltrados.stream()
                       .map(Hecho::getTitulo)
                       .toList()
    );
  }
}
