package ar.edu.utn.frba.dds;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ar.edu.utn.frba.dds.domain.algoritmosconsenso.Absoluta;
import ar.edu.utn.frba.dds.domain.algoritmosconsenso.AlgoritmoDeConsenso;
import ar.edu.utn.frba.dds.domain.coleccion.Coleccion;
import ar.edu.utn.frba.dds.domain.detectorspam.DetectorSpam;
import ar.edu.utn.frba.dds.domain.filtro.Filtro;
import ar.edu.utn.frba.dds.domain.fuentes.Fuente;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.hecho.HechoBuilder;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.origen.Origen;
import ar.edu.utn.frba.dds.domain.reportes.RepositorioDeSolicitudes;
import ar.edu.utn.frba.dds.domain.serializadores.Serializador;
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
  PuntoGeografico pgAux = new PuntoGeografico(33.39627891281455, 44.48695991794239);
  FuenteDinamica fuenteAuxD;
  LocalDateTime horaAux = LocalDateTime.now().minusDays(1);
  List<String> etiquetasAux = List.of(
      "#ancianita",
      "#robo_a_mano_armada",
      "#violencia",
      "#leyDeProtecciónALasAncianitas",
      "#NOalaVIOLENCIAcontraABUELITAS"
  );
  private RepositorioDeSolicitudes repositorio;
  private DetectorSpam detectorSpam;
  private AlgoritmoDeConsenso absoluta;
  private Path tempJsonFile;

  @Mock
  private Serializador<Hecho> serializadorMock;

  @BeforeEach
  void initFileSystem() throws IOException {
    MockitoAnnotations.openMocks(this);
    detectorSpam = mock(DetectorSpam.class);
    repositorio = new RepositorioDeSolicitudes(detectorSpam);
    absoluta = new Absoluta();
    tempJsonFile = Files.createTempFile("test_fuente_dinamica_", ".json");
    when(serializadorMock.importar(anyString())).thenReturn(new ArrayList<>());
    fuenteAuxD = new FuenteDinamica("Julio Cesar", tempJsonFile.toString(), serializadorMock);
  }

  @AfterEach
  void cleanupFileSystem() throws IOException {
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
        .conDescripcion("desc")
        .conCategoria("Robos")
        .conDireccion("direccion")
        .conProvincia("Provincia")
        .conUbicacion(null)
        .conFechaSuceso(horaAux)
        .conFechaCarga(LocalDateTime.now())
        .conFuenteOrigen(Origen.DATASET)
        .conEtiquetas(etiquetasAux)
        .build();
    fuenteAuxD.agregarHecho(hecho);
    assertTrue(coleccion.contieneA(hecho, repositorio));
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
        .conDescripcion("desc")
        .conCategoria("Robos")
        .conDireccion("direccion")
        .conProvincia("Provincia")
        .conUbicacion(pgAux)
        .conFechaSuceso(horaAux)
        .conFechaCarga(LocalDateTime.now())
        .conFuenteOrigen(Origen.PROVISTO_CONTRIBUYENTE)
        .conEtiquetas(etiquetasAux)
        .build();
    fuenteAuxD.agregarHecho(hecho);
    when(detectorSpam.esSpam(anyString())).thenReturn(false);
    repositorio.marcarComoEliminado(hecho);
    assertFalse(coleccion.contieneA(hecho, repositorio));
  }

  @Test
  public void coleccionContieneFuenteCorrecta() {
    Coleccion coleccion = new Coleccion("Robos", fuenteAuxD, "Descripcion", "Robos");
    assertTrue(coleccion.contieneFuente(fuenteAuxD));
  }

  @Test
  public void testFiltradoYSpamDetectadoCorrectamente() {
    Fuente fuente = mock(Fuente.class);
    Hecho valido = mock(Hecho.class);
    Hecho spam = mock(Hecho.class);
    when(fuente.obtenerHechos()).thenReturn(List.of(valido, spam));

    Coleccion coleccion = new Coleccion("Test", fuente, "Descripcion", "Categoria");

    Filtro filtroMock = mock(Filtro.class);
    when(filtroMock.filtrar(anyList())).thenReturn(List.of(valido));
    coleccion.setFiltro(filtroMock);

    RepositorioDeSolicitudes localRepositorioMock = mock(RepositorioDeSolicitudes.class);
    Filtro filtroExcluyente = mock(Filtro.class);

    when(localRepositorioMock.filtroExcluyente()).thenReturn(filtroExcluyente);
    when(filtroExcluyente.filtrar(anyList())).thenReturn(List.of(valido));

    List<Hecho> hechosFinales = coleccion.getHechos(localRepositorioMock);

    assertEquals(1, hechosFinales.size());
    assertTrue(hechosFinales.contains(valido));
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

    when(fuente.obtenerHechos()).thenReturn(List.of(hecho1, hecho2));

    List<Hecho> hechosActualizados = coleccion.getHechos(repositorio);
    assertEquals(2, hechosActualizados.size());
    assertTrue(hechosActualizados.contains(hecho1));
    assertTrue(hechosActualizados.contains(hecho2));
  }
}
