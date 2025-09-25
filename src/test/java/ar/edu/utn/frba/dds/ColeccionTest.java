package ar.edu.utn.frba.dds;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ar.edu.utn.frba.dds.domain.coleccion.Coleccion;
import ar.edu.utn.frba.dds.domain.filtro.Filtro;
import ar.edu.utn.frba.dds.domain.filtro.condiciones.condicion.Condicion;
import ar.edu.utn.frba.dds.domain.filtro.condiciones.condicion.CondicionGenerica;
import ar.edu.utn.frba.dds.domain.fuentes.Fuente;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.hecho.HechoBuilder;
import ar.edu.utn.frba.dds.domain.reportes.RepositorioDeSolicitudes;
import ar.edu.utn.frba.dds.domain.reportes.detectorspam.DetectorSpam;
import ar.edu.utn.frba.dds.domain.exportador.Exportador;
import ar.edu.utn.frba.dds.domain.lector.Lector;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ColeccionTest {
  FuenteDinamica fuenteAuxD;
  LocalDateTime horaAux = LocalDateTime.now()
                                       .minusDays(1);
  private RepositorioDeSolicitudes repositorio;
  private Path tempJsonFile;

  @Mock
  private Lector<Hecho> lectorMock;
  @Mock
  private Exportador<Hecho> exportadorMock;
  @Mock
  private DetectorSpam detectorSpam;

  @BeforeEach
  void init() throws IOException {
    MockitoAnnotations.openMocks(this);
    repositorio = new RepositorioDeSolicitudes(detectorSpam);
    tempJsonFile = Files.createTempFile("test_fuente_dinamica_", ".json");
    when(lectorMock.importar(anyString())).thenReturn(new ArrayList<>());
    fuenteAuxD = new FuenteDinamica(
        "Julio Cesar",
        tempJsonFile.toString(),
        lectorMock,
        exportadorMock
    );
  }

  @AfterEach
  void cleanup() throws IOException {
    Files.deleteIfExists(tempJsonFile);
  }

  @Test
  public void coleccionCreadaCorrectamente() {
    Coleccion bonaerense = new Coleccion(
        "Robos",
        fuenteAuxD,
        "Un día más siendo del conurbano",
        "Robos"
    );

    assertEquals("Robos", bonaerense.getTitulo());
    assertEquals("Un día más siendo del conurbano", bonaerense.getDescripcion());
    assertEquals("Robos", bonaerense.getCategoria());
  }

  @Test
  public void coleccionContieneUnHecho() {
    Coleccion coleccion = new Coleccion("Robos", fuenteAuxD, "Descripcion", "Robos");
    Hecho hecho = new HechoBuilder()
        .conTitulo("titulo")
        .conFechaSuceso(horaAux)
        .build();
    fuenteAuxD.agregarHecho(hecho);
    assertTrue(coleccion.incluyeHecho(hecho, repositorio));
  }

  @Test
  public void coleccionEsDeCategoriaCorrectamente() {
    Coleccion coleccion = new Coleccion("Robos", fuenteAuxD, "Descripcion", "Robos");
    assertEquals("Robos", coleccion.getCategoria());
    assertNotEquals("Violencia", coleccion.getCategoria());
  }

  @Test
  public void siCreoUnaColeccionSinTituloLanzaExcepcion() {
    assertThrows(RuntimeException.class, () -> new Coleccion("", fuenteAuxD, "hola", "Robos"));
  }

  @Test
  public void siCreoUnaColeccionSinDescripcionLanzaExcepcion() {
    assertThrows(RuntimeException.class, () -> new Coleccion("Robos", fuenteAuxD, "", "Robos"));
  }

  @Test
  public void siCreoUnaColeccionSinCategoriaLanzaExcepcion() {
    assertThrows(RuntimeException.class, () -> new Coleccion("Robos", fuenteAuxD, "hola", ""));
  }

  @Test
  public void nombreColeccionNoEsNull() {
    Coleccion coleccion = new Coleccion("Robos", fuenteAuxD, "Descripcion", "Robos");
    assertNotNull(coleccion.getTitulo());
  }

  @Test
  public void coleccionYaNoContieneHechoEliminadoPorGestor() {
    Coleccion coleccion = new Coleccion("Robos", fuenteAuxD, "Descripcion", "Robos");
    Hecho hecho = new HechoBuilder()
        .conTitulo("titulo")
        .conFechaSuceso(horaAux)
        .build();
    fuenteAuxD.agregarHecho(hecho);
    when(detectorSpam.esSpam(anyString())).thenReturn(false);
    repositorio.marcarComoEliminado(hecho);
    assertFalse(coleccion.incluyeHecho(hecho, repositorio));
  }

  @Test
  public void coleccionContieneFuenteCorrecta() {
    Coleccion coleccion = new Coleccion("Robos", fuenteAuxD, "Descripcion", "Robos");
    assertTrue(coleccion.contieneFuente(fuenteAuxD));
  }

  @Test
  public void testFiltradoYSpamDetectadoCorrectamente() {
    // Arrange: Preparamos los datos y mocks
    Fuente fuente = mock(Fuente.class);
    Hecho valido = new HechoBuilder().conTitulo("valido")
                                     .conFechaSuceso(LocalDateTime.now()
                                                                  .minusDays(1))
                                     .build();
    Hecho spam = new HechoBuilder().conTitulo("spam")
                                   .conFechaSuceso(LocalDateTime.now()
                                                                .minusDays(1))
                                   .build();
    Hecho filtradoPorColeccion = new HechoBuilder().conTitulo("filtrado")
                                                   .conFechaSuceso(LocalDateTime.now()
                                                                                .minusDays(1))
                                                   .build();

    when(fuente.obtenerHechos()).thenReturn(List.of(valido, spam, filtradoPorColeccion));

    Coleccion coleccion = new Coleccion("Test", fuente, "Descripcion", "Categoria");

    // 1. Configurar el filtro propio de la colección para que excluya a "filtradoPorColeccion"
    Condicion condicionColeccion = new CondicionGenerica("titulo", "DISTINTO", "filtrado");
    coleccion.setCondicion(condicionColeccion);

    // 2. Configurar el mock del repositorio para que su filtro excluyente elimine a "spam"
    RepositorioDeSolicitudes repoMock = mock(RepositorioDeSolicitudes.class);
    Condicion condicionSpam = new CondicionGenerica("titulo", "DISTINTO", "spam");
    Filtro filtroExcluyente = new Filtro(condicionSpam);
    when(repoMock.filtroExcluyente()).thenReturn(filtroExcluyente);

    // Act: Ejecutamos el método a probar
    List<Hecho> hechosFinales = coleccion.getHechos(repoMock);

    // Assert: Verificamos que el resultado sea el esperado
    assertEquals(1, hechosFinales.size());
    assertTrue(hechosFinales.contains(valido));
    assertFalse(hechosFinales.contains(spam));
    assertFalse(hechosFinales.contains(filtradoPorColeccion));
  }


  @Test
  public void testHechosCambianConFuente() {
    Fuente fuente = mock(Fuente.class);
    Hecho hecho1 = mock(Hecho.class);
    Hecho hecho2 = mock(Hecho.class);

    when(fuente.obtenerHechos()).thenReturn(List.of(hecho1));

    Coleccion coleccion = new Coleccion("Test", fuente, "Descripcion", "Categoria");

    List<Hecho> hechosIniciales = coleccion.getHechos(repositorio);
    assertEquals(1, hechosIniciales.size());
    assertTrue(hechosIniciales.contains(hecho1));

    // La fuente ahora devuelve más hechos
    when(fuente.obtenerHechos()).thenReturn(List.of(hecho1, hecho2));

    List<Hecho> hechosActualizados = coleccion.getHechos(repositorio);
    assertEquals(2, hechosActualizados.size());
    assertTrue(hechosActualizados.containsAll(List.of(hecho1, hecho2)));
  }
}
