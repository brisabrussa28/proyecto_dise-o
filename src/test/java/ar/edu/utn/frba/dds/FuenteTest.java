package ar.edu.utn.frba.dds;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ar.edu.utn.frba.dds.domain.csv.LectorCSV;
import ar.edu.utn.frba.dds.domain.fuentes.Fuente;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDeAgregacion;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteEstatica;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.AfterEach; // Import for cleanup after each test
import org.junit.jupiter.api.BeforeEach; // Import for setup before each test
import org.junit.jupiter.api.Test;

public class FuenteTest {

  // Common setup for FuenteDeAgregacion tests
  private Path tempJsonFilePathAgregacion;

  @BeforeEach
  void setUpCommon() throws IOException {
    // Create a temporary JSON file for FuenteDeAgregacion tests
    tempJsonFilePathAgregacion = Files.createTempFile("test_agregacion", ".json");
  }

  @AfterEach
  void tearDownCommon() throws IOException {
    // Clean up the temporary JSON file after FuenteDeAgregacion tests
    Files.deleteIfExists(tempJsonFilePathAgregacion);
  }

  // Tests FuenteDinamica

  @Test
  public void fuenteDinamicaIniciaConListaVaciaSiNoSeProporciona() {
    FuenteDinamica fuente = new FuenteDinamica("MiFuente", null);
    assertTrue(fuente.obtenerHechos()
        .isEmpty());
  }

  @Test
  public void fuenteDinamicaAgregaHechoCorrectamente() {
    FuenteDinamica fuente = new FuenteDinamica("Fuente1", null);
    Hecho hechoMock = mock(Hecho.class);
    fuente.agregarHecho(hechoMock);
    assertEquals(
        1,
        fuente.obtenerHechos()
            .size()
    );
    assertEquals(
        hechoMock,
        fuente.obtenerHechos()
            .get(0)
    );
  }

  @Test
  public void obtenerHechosDevuelveListaInmutableDinamica() {
    FuenteDinamica fuente = new FuenteDinamica("FuenteInmutable", null);
    Hecho hecho = mock(Hecho.class);
    fuente.agregarHecho(hecho);
    List<Hecho> hechos = fuente.obtenerHechos();
    assertThrows(UnsupportedOperationException.class, () -> hechos.add(mock(Hecho.class)));
  }

  @Test
  public void fuenteDinamicaPuedeIniciarseConHechos() {
    Hecho hecho = mock(Hecho.class);
    FuenteDinamica fuente = new FuenteDinamica("FuenteConHechos", List.of(hecho));
    assertEquals(
        1,
        fuente.obtenerHechos()
            .size()
    );
    assertTrue(fuente.obtenerHechos()
        .contains(hecho));
  }

  @Test
  public void agregarHechoNoAgregaNulo() {
    FuenteDinamica fuente = new FuenteDinamica("Fuente", null);
    fuente.agregarHecho(null);
    assertEquals(
        1,
        fuente.obtenerHechos()
            .size()
    );
    assertNull(fuente.obtenerHechos()
        .get(0));
  }

  // Tests FuenteEstatica

  @Test
  public void fuenteEstaticaUsaLectorCSVCorrectamente() {
    Hecho hechoMock = mock(Hecho.class);
    LectorCSV lectorMock = mock(LectorCSV.class);

    // Adjust arguments according to changes in the import method
    when(lectorMock.importar("ruta.csv")).thenReturn(List.of(hechoMock));

    // Adjust FuenteEstatica constructor if it changed
    FuenteEstatica fuente = new FuenteEstatica("MiFuente", "ruta.csv", lectorMock);

    List<Hecho> hechos = fuente.obtenerHechos();

    assertEquals(1, hechos.size());
    assertEquals(hechoMock, hechos.get(0));
    verify(lectorMock).importar("ruta.csv");
  }

  @Test
  public void fuenteEstaticaDevuelveListaVaciaSiCSVEstaVacio() {
    LectorCSV lectorMock = mock(LectorCSV.class);

    // Adjust arguments according to changes in the import method
    when(lectorMock.importar(anyString())).thenReturn(List.of());

    // Adjust FuenteEstatica constructor if it changed
    FuenteEstatica fuente = new FuenteEstatica("Vacia", "vac.csv", lectorMock);

    List<Hecho> hechos = fuente.obtenerHechos();

    assertTrue(hechos.isEmpty());
  }

  // Tests FuenteDeAgregacion

  @Test
  public void fuenteDeAgregacionCombinaHechosDeTodasLasFuentes() {
    Hecho hecho1 = mock(Hecho.class);
    Hecho hecho2 = mock(Hecho.class);
    Fuente fuente1 = mock(Fuente.class);
    Fuente fuente2 = mock(Fuente.class);
    when(fuente1.obtenerHechos()).thenReturn(List.of(hecho1));
    when(fuente2.obtenerHechos()).thenReturn(List.of(hecho2));

    // Update constructor call for FuenteDeAgregacion
    FuenteDeAgregacion agregadora = new FuenteDeAgregacion("Agregada", tempJsonFilePathAgregacion.toString());
    agregadora.agregarFuente(fuente1);
    agregadora.agregarFuente(fuente2);
    // Explicitly call to update the cache for the test
    agregadora.actualizarHechosAgregadosYGuardarCopia(); // ADDED THIS LINE

    List<Hecho> todos = agregadora.obtenerHechos();
    assertEquals(2, todos.size());
    assertTrue(todos.contains(hecho1));
    assertTrue(todos.contains(hecho2));

    // Stop the scheduler after the test
    agregadora.detenerScheduler();
  }

  @Test
  public void fuenteDeAgregacionConFuentesVacias() {
    Fuente vacia1 = mock(Fuente.class);
    Fuente vacia2 = mock(Fuente.class);
    when(vacia1.obtenerHechos()).thenReturn(List.of());
    when(vacia2.obtenerHechos()).thenReturn(List.of());

    // Update constructor call for FuenteDeAgregacion
    FuenteDeAgregacion agregadora = new FuenteDeAgregacion("AgregadoraVacia", tempJsonFilePathAgregacion.toString());
    agregadora.agregarFuente(vacia1);
    agregadora.agregarFuente(vacia2);
    // Explicitly call to update the cache for the test
    agregadora.actualizarHechosAgregadosYGuardarCopia(); // ADDED THIS LINE
    assertTrue(agregadora.obtenerHechos()
        .isEmpty());

    // Stop the scheduler after the test
    agregadora.detenerScheduler();
  }

  @Test
  public void fuenteDeAgregacionAdmiteFuentesDinamicamente() {
    Fuente fuente = mock(Fuente.class);
    when(fuente.obtenerHechos()).thenReturn(List.of(mock(Hecho.class)));

    // Update constructor call for FuenteDeAgregacion
    FuenteDeAgregacion agregadora = new FuenteDeAgregacion("Dinámica", tempJsonFilePathAgregacion.toString());
    agregadora.agregarFuente(fuente);
    // Explicitly call to update the cache for the test
    agregadora.actualizarHechosAgregadosYGuardarCopia(); // ADDED THIS LINE
    assertEquals(
        1,
        agregadora.obtenerHechos()
            .size()
    );

    // Stop the scheduler after the test
    agregadora.detenerScheduler();
  }
}
