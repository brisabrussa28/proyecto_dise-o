package ar.edu.utn.frba.dds;

import static ar.edu.utn.frba.dds.domain.filtro.condiciones.Operador.DISTINTO;
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
import ar.edu.utn.frba.dds.domain.filtro.condiciones.Condicion;
import ar.edu.utn.frba.dds.domain.filtro.condiciones.CondicionGenerica;
import ar.edu.utn.frba.dds.domain.fuentes.Fuente;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.hecho.HechoBuilder;
import ar.edu.utn.frba.dds.domain.reportes.RepositorioDeSolicitudes;
import ar.edu.utn.frba.dds.domain.reportes.detectorspam.DetectorSpam;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ColeccionTest {
  private FuenteDinamica fuente;
  private RepositorioDeSolicitudes repositorio;
  private final LocalDateTime horaAux = LocalDateTime.now().minusDays(1);

  @Mock
  private DetectorSpam detectorSpam;

  @BeforeEach
  void init() {
    MockitoAnnotations.openMocks(this);
    repositorio = new RepositorioDeSolicitudes(detectorSpam);
    // CORRECCIÓN: FuenteDinamica ahora tiene un constructor simple.
    // Ya no necesita mocks de Lector, Exportador ni archivos temporales.
    fuente = new FuenteDinamica("Fuente para Colecciones");
  }

  @Test
  public void coleccionCreadaCorrectamente() {
    Coleccion bonaerense = new Coleccion(
        "Robos",
        fuente,
        "Un día más siendo del conurbano",
        "Robos"
    );

    assertEquals("Robos", bonaerense.getTitulo());
    assertEquals("Un día más siendo del conurbano", bonaerense.getDescripcion());
    assertEquals("Robos", bonaerense.getCategoria());
  }

  @Test
  public void coleccionContieneUnHecho() {
    Coleccion coleccion = new Coleccion("Robos", fuente, "Descripcion", "Robos");
    Hecho hecho = new HechoBuilder().conTitulo("titulo").conFechaSuceso(horaAux).build();
    fuente.agregarHecho(hecho);
    assertTrue(coleccion.contieneA(hecho, repositorio));
  }

  @Test
  public void coleccionEsDeCategoriaCorrectamente() {
    Coleccion coleccion = new Coleccion("Robos", fuente, "Descripcion", "Robos");
    assertEquals("Robos", coleccion.getCategoria());
    assertNotEquals("Violencia", coleccion.getCategoria());
  }

  @Test
  public void siCreoUnaColeccionSinTituloLanzaExcepcion() {
    assertThrows(RuntimeException.class, () -> new Coleccion("", fuente, "hola", "Robos"));
  }

  @Test
  public void siCreoUnaColeccionSinDescripcionLanzaExcepcion() {
    assertThrows(RuntimeException.class, () -> new Coleccion("Robos", fuente, "", "Robos"));
  }

  @Test
  public void siCreoUnaColeccionSinCategoriaLanzaExcepcion() {
    assertThrows(RuntimeException.class, () -> new Coleccion("Robos", fuente, "hola", ""));
  }

  @Test
  public void nombreColeccionNoEsNull() {
    Coleccion coleccion = new Coleccion("Robos", fuente, "Descripcion", "Robos");
    assertNotNull(coleccion.getTitulo());
  }

  @Test
  public void coleccionYaNoContieneHechoEliminadoPorGestor() {
    Coleccion coleccion = new Coleccion("Robos", fuente, "Descripcion", "Robos");
    Hecho hecho = new HechoBuilder().conTitulo("titulo").conFechaSuceso(horaAux).build();
    fuente.agregarHecho(hecho);
    when(detectorSpam.esSpam(anyString())).thenReturn(false);
    repositorio.marcarComoEliminado(hecho);
    assertFalse(coleccion.contieneA(hecho, repositorio));
  }

  @Test
  public void coleccionContieneFuenteCorrecta() {
    Coleccion coleccion = new Coleccion("Robos", fuente, "Descripcion", "Robos");
    assertTrue(coleccion.contieneFuente(fuente));
  }

  @Test
  public void testFiltradoYSpamDetectadoCorrectamente() {
    // Arrange
    Fuente fuenteMock = mock(Fuente.class);
    Hecho valido = new HechoBuilder().conTitulo("valido").conFechaSuceso(horaAux).build();
    Hecho spam = new HechoBuilder().conTitulo("spam").conFechaSuceso(horaAux).build();
    Hecho filtrado = new HechoBuilder().conTitulo("filtrado").conFechaSuceso(horaAux).build();
    when(fuenteMock.obtenerHechos()).thenReturn(List.of(valido, spam, filtrado));

    Coleccion coleccion = new Coleccion("Test", fuenteMock, "Descripcion", "Categoria");
    coleccion.setCondicion(new CondicionGenerica("titulo", DISTINTO, "filtrado"));

    RepositorioDeSolicitudes repoMock = mock(RepositorioDeSolicitudes.class);
    Filtro filtroExcluyente = new Filtro(new CondicionGenerica("titulo", DISTINTO, "spam"));
    when(repoMock.filtroExcluyente()).thenReturn(filtroExcluyente);

    // Act
    List<Hecho> hechosFinales = coleccion.getHechos(repoMock);

    // Assert
    assertEquals(1, hechosFinales.size());
    assertTrue(hechosFinales.contains(valido));
    assertFalse(hechosFinales.contains(spam));
    assertFalse(hechosFinales.contains(filtrado));
  }

  @Test
  public void testHechosCambianConFuente() {
    Fuente fuenteMock = mock(Fuente.class);
    Hecho hecho1 = mock(Hecho.class);
    Hecho hecho2 = mock(Hecho.class);
    when(fuenteMock.obtenerHechos()).thenReturn(List.of(hecho1));

    Coleccion coleccion = new Coleccion("Test", fuenteMock, "Descripcion", "Categoria");
    assertEquals(1, coleccion.getHechos(repositorio).size());

    // La fuente ahora devuelve más hechos
    when(fuenteMock.obtenerHechos()).thenReturn(List.of(hecho1, hecho2));
    assertEquals(2, coleccion.getHechos(repositorio).size());
  }
}