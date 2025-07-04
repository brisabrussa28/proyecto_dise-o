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
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.origen.Origen;
import ar.edu.utn.frba.dds.domain.reportes.RepositorioDeSolicitudes;
import java.io.IOException; // Importar IOException
import java.nio.file.Files; // Importar Files
import java.nio.file.Path; // Importar Path
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach; // Importar AfterEach
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ColeccionTest {
  PuntoGeografico pgAux = new PuntoGeografico(33.39627891281455, 44.48695991794239);
  // FuenteDinamica se inicializará en setUp para asegurar un estado limpio en cada test
  FuenteDinamica fuenteAuxD;
  LocalDateTime horaAux = LocalDateTime.of(2025, 5, 6, 20, 9);
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
  private Path tempJsonFile; // Declarar la variable para el archivo temporal

  @BeforeEach
  void initFileSystem() throws IOException { // Añadir throws IOException
    // IMPORTANT: Ensure detectorSpam is initialized BEFORE repositorio, as repositorio depends on it.
    detectorSpam = mock(DetectorSpam.class);
    repositorio = new RepositorioDeSolicitudes(detectorSpam);
    absoluta = new Absoluta();
    // Initialize FuenteDinamica with a temporary JSON file path
    tempJsonFile = Files.createTempFile("test_fuente_dinamica_", ".json"); // Crear archivo temporal
    fuenteAuxD = new FuenteDinamica("Julio Cesar", tempJsonFile.toString());
  }

  @AfterEach
  void cleanupFileSystem() throws IOException { // Método para limpiar el archivo temporal
    Files.deleteIfExists(tempJsonFile);
  }


  @Test
  public void coleccionCreadaCorrectamente() {
    // Se usa el constructor de Coleccion directamente
    Coleccion bonaerense = new Coleccion(
        "Robos",
        fuenteAuxD, // Se pasa la instancia de FuenteDinamica
        "Un día más siendo del conurbano",
        "Robos"
    );

    assertEquals("Robos", bonaerense.getTitulo());
    assertEquals("Un día más siendo del conurbano", bonaerense.getDescripcion());
    assertEquals("Robos", bonaerense.getCategoria());
  }

  @Test
  public void coleccionContieneUnHecho() {
    // Se usa el constructor de Coleccion directamente
    Coleccion coleccion = new Coleccion("Robos", fuenteAuxD, "Descripcion", "Robos");
    Hecho hecho = new Hecho(
        "titulo",
        "desc",
        "Robos",
        "direccion",
        null,
        horaAux,
        LocalDateTime.now(), // Se añade fechaCarga
        null,
        etiquetasAux
    );
    fuenteAuxD.agregarHecho(hecho);
    assertTrue(coleccion.contieneA(hecho, repositorio));
  }

  @Test
  public void coleccionEsDeCategoriaCorrectamente() {
    // Se usa el constructor de Coleccion directamente
    Coleccion coleccion = new Coleccion("Robos", fuenteAuxD, "Descripcion", "Robos");
    assertEquals("Robos", coleccion.getCategoria());
    assertNotEquals("Violencia", coleccion.getCategoria());
  }

  @Test
  public void siCreoUnaColeccionSinTituloLanzaExcepcion() {
    // Se usa el constructor de Coleccion directamente
    assertThrows(RuntimeException.class, () -> new Coleccion("", fuenteAuxD, "hola", "Robos"));
  }

  @Test
  public void siCreoUnaColeccionSinDescripcionLanzaExcepcion() {
    // Se usa el constructor de Coleccion directamente
    assertThrows(RuntimeException.class, () -> new Coleccion("Robos", fuenteAuxD, "", "Robos"));
  }

  @Test
  public void siCreoUnaColeccionSinCategoriaLanzaExcepcion() {
    // Se usa el constructor de Coleccion directamente
    assertThrows(RuntimeException.class, () -> new Coleccion("Robos", fuenteAuxD, "hola", ""));
  }

  @Test
  public void nombreColeccionNoEsNull() {
    // Se usa el constructor de Coleccion directamente
    Coleccion coleccion = new Coleccion("Robos", fuenteAuxD, "Descripcion", "Robos");
    assertNotNull(coleccion.getTitulo());
  }

  @Test
  public void coleccionYaNoContieneHechoEliminadoPorGestor() {
    // Se usa el constructor de Coleccion directamente
    Coleccion coleccion = new Coleccion("Robos", fuenteAuxD, "Descripcion", "Robos");
    Hecho hecho = new Hecho(
        "titulo",
        "desc",
        "Robos",
        "direccion",
        pgAux,
        horaAux,
        LocalDateTime.now(), // Se añade fechaCarga
        Origen.PROVISTO_CONTRIBUYENTE,
        etiquetasAux
    );
    fuenteAuxD.agregarHecho(hecho);
    when(detectorSpam.esSpam(anyString())).thenReturn(false);
    repositorio.marcarComoEliminado(hecho);
    assertFalse(coleccion.contieneA(hecho, repositorio));
  }

  @Test
  public void coleccionContieneFuenteCorrecta() {
    // Se usa el constructor de Coleccion directamente
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

    // Se crea un mock local de RepositorioDeSolicitudes para esta prueba
    RepositorioDeSolicitudes localRepositorioMock = mock(RepositorioDeSolicitudes.class);
    Filtro filtroExcluyente = mock(Filtro.class);

    // Se stubea el mock local para que devuelva el filtroExcluyente mockeado
    when(localRepositorioMock.filtroExcluyente()).thenReturn(filtroExcluyente);
    when(filtroExcluyente.filtrar(anyList())).thenReturn(List.of(valido)); // Esta es la línea 178

    // Se llama al método real con el mock local
    List<Hecho> hechosFinales = coleccion.getHechos(localRepositorioMock);

    assertEquals(1, hechosFinales.size());
    assertTrue(hechosFinales.contains(valido));
  }

  @Test
  public void testHechosCambianConFuente() {
    Fuente fuente = mock(Fuente.class);
    Hecho hecho1 = mock(Hecho.class);
    Hecho hecho2 = mock(Hecho.class);
    // RepositorioDeSolicitudes repositorio is already initialized in @BeforeEach

    when(fuente.obtenerHechos()).thenReturn(List.of(hecho1));

    Coleccion coleccion = new Coleccion("Test", fuente, "Descripcion", "Categoria");

    // Obtener los hechos la primera vez
    List<Hecho> hechosIniciales = coleccion.getHechos(repositorio);
    assertEquals(1, hechosIniciales.size());
    assertTrue(hechosIniciales.contains(hecho1));

    // Cambiar el comportamiento de la fuente para que devuelva más hechos
    when(fuente.obtenerHechos()).thenReturn(List.of(hecho1, hecho2));

    // Obtener los hechos nuevamente y verificar que se actualicen
    List<Hecho> hechosActualizados = coleccion.getHechos(repositorio);
    assertEquals(2, hechosActualizados.size());
    assertTrue(hechosActualizados.contains(hecho1));
    assertTrue(hechosActualizados.contains(hecho2));
  }
}
