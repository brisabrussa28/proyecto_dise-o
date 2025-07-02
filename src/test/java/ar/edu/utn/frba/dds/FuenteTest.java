package ar.edu.utn.frba.dds;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class FuenteTest {

  // --- Pruebas para FuenteDinamica ---
  @Nested
  @DisplayName("Pruebas para FuenteDinamica")
  class FuenteDinamicaTests {
    private FuenteDinamica fuente;

    @BeforeEach
    void setUp() {
      fuente = new FuenteDinamica("MiFuente", null);
    }

    @Test
    public void iniciaConListaVaciaSiNoSeProporciona() {
      assertTrue(fuente.obtenerHechos().isEmpty());
    }

    @Test
    public void agregaHechoCorrectamente() {
      Hecho hechoMock = mock(Hecho.class);
      fuente.agregarHecho(hechoMock);
      assertEquals(1, fuente.obtenerHechos().size());
      assertEquals(hechoMock, fuente.obtenerHechos().get(0));
    }

    @Test
    public void obtenerHechosDevuelveListaInmutable() {
      fuente.agregarHecho(mock(Hecho.class));
      List<Hecho> hechos = fuente.obtenerHechos();
      assertThrows(UnsupportedOperationException.class, () -> hechos.add(mock(Hecho.class)));
    }

    @Test
    public void puedeIniciarseConHechos() {
      Hecho hecho = mock(Hecho.class);
      FuenteDinamica fuenteConHechos = new FuenteDinamica("FuenteConHechos", List.of(hecho));
      assertEquals(1, fuenteConHechos.obtenerHechos().size());
      assertTrue(fuenteConHechos.obtenerHechos().contains(hecho));
    }
  }

  // --- Pruebas para FuenteEstatica ---
  @Nested
  @DisplayName("Pruebas para FuenteEstatica")
  class FuenteEstaticaTests {
    @Test
    public void usaLectorCSVCorrectamente() {
      Hecho hechoMock = mock(Hecho.class);
      LectorCSV lectorMock = mock(LectorCSV.class);
      when(lectorMock.importar("ruta.csv")).thenReturn(List.of(hechoMock));
      FuenteEstatica fuente = new FuenteEstatica("MiFuente", "ruta.csv", lectorMock);

      List<Hecho> hechos = fuente.obtenerHechos();

      assertEquals(1, hechos.size());
      assertEquals(hechoMock, hechos.get(0));
      verify(lectorMock).importar("ruta.csv");
    }

    @Test
    public void devuelveListaVaciaSiCSVEstaVacio() {
      LectorCSV lectorMock = mock(LectorCSV.class);
      when(lectorMock.importar(anyString())).thenReturn(List.of());
      FuenteEstatica fuente = new FuenteEstatica("Vacia", "vac.csv", lectorMock);
      assertTrue(fuente.obtenerHechos().isEmpty());
    }
  }

  // --- Pruebas para FuenteDeAgregacion (Refactorizada) ---
  @Nested
  @DisplayName("Pruebas para FuenteDeAgregacion")
  class FuenteDeAgregacionTests {
    private Path tempJsonFile;
    private FuenteDeAgregacion agregadora;
    private Fuente fuenteMock1;
    private Fuente fuenteMock2;
    private Hecho hechoMock1;
    private Hecho hechoMock2;

    @BeforeEach
    void setUp() throws IOException {
      tempJsonFile = Files.createTempFile("test_agregacion_", ".json");
      // FIX: Se usa el constructor correcto con nombre y ruta.
      agregadora = new FuenteDeAgregacion("TestAgregadora", tempJsonFile.toString());

      fuenteMock1 = mock(Fuente.class);
      fuenteMock2 = mock(Fuente.class);
      hechoMock1 = mock(Hecho.class);
      hechoMock2 = mock(Hecho.class);
    }

    @AfterEach
    void tearDown() throws IOException {
      // FIX: Se detiene el scheduler para una limpieza correcta.
      agregadora.detenerScheduler();
      Files.deleteIfExists(tempJsonFile);
    }

    @Test
    @DisplayName("Debe iniciar con una lista vacía si no existe caché")
    public void iniciaVaciaSiNoHayCache() {
      assertTrue(agregadora.obtenerHechos().isEmpty(), "La lista de hechos debería estar vacía al inicio.");
    }

    @Test
    @DisplayName("Debe combinar hechos de múltiples fuentes tras una actualización")
    public void combinaHechosDeVariasFuentes() {
      when(fuenteMock1.obtenerHechos()).thenReturn(List.of(hechoMock1));
      when(fuenteMock2.obtenerHechos()).thenReturn(List.of(hechoMock2));
      agregadora.agregarFuente(fuenteMock1);
      agregadora.agregarFuente(fuenteMock2);

      agregadora.forzarActualizacionSincrona();

      List<Hecho> todos = agregadora.obtenerHechos();
      assertEquals(2, todos.size(), "Debería haber 2 hechos en total.");
      assertTrue(todos.containsAll(List.of(hechoMock1, hechoMock2)), "Debería contener los hechos de ambas fuentes.");
    }
  }
}
