package ar.edu.utn.frba.dds;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ar.edu.utn.frba.dds.domain.calendarizacion.App;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDeCopiaLocal;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class AppTest {

  private App app;

  @BeforeEach
  void setUp() {
    app = new App();
  }

  @AfterEach
  void tearDown() throws IOException {
    // Limpia los archivos que podrían ser creados por el test de integración
    Files.deleteIfExists(Paths.get("agregados.json"));
    Files.deleteIfExists(Paths.get("dinamica.json"));
  }

  @Test
  @DisplayName("Una fuente puede ser registrada correctamente")
  void registrarFuenteCorrectamente() {
    FuenteDeCopiaLocal mockFuente = mock(FuenteDeCopiaLocal.class);
    when(mockFuente.getNombre()).thenReturn("TestFuente");

    app.registrarFuente(mockFuente);
    Map<String, FuenteDeCopiaLocal> fuentes = app.getFuentesRegistradas();

    assertNotNull(fuentes);
    assertEquals(1, fuentes.size());
    assertTrue(fuentes.containsKey("TestFuente"));
    assertEquals(mockFuente, fuentes.get("TestFuente"));
  }

  @Test
  @DisplayName("Registrar una fuente nula no debe agregarla ni lanzar excepción")
  void registrarFuenteNula() {
    app.registrarFuente(null);
    Map<String, FuenteDeCopiaLocal> fuentes = app.getFuentesRegistradas();

    assertNotNull(fuentes);
    assertTrue(fuentes.isEmpty());
  }

  @Test
  @DisplayName("La actualización se ejecuta para todas las fuentes registradas")
  void ejecutarActualizacionParaTodasLasFuentes() {
    FuenteDeCopiaLocal mockFuente1 = mock(FuenteDeCopiaLocal.class);
    when(mockFuente1.getNombre()).thenReturn("Fuente1");
    FuenteDeCopiaLocal mockFuente2 = mock(FuenteDeCopiaLocal.class);
    when(mockFuente2.getNombre()).thenReturn("Fuente2");

    app.registrarFuente(mockFuente1);
    app.registrarFuente(mockFuente2);

    app.ejecutarActualizacionTodasLasFuentes();

    verify(mockFuente1, times(1)).forzarActualizacionSincrona();
    verify(mockFuente2, times(1)).forzarActualizacionSincrona();
  }

  @Test
  @DisplayName("La configuración de la aplicación registra las fuentes por defecto")
  void configurarAplicacionRegistraFuentes() {
    App configuredApp = App.configurarAplicacion();
    assertNotNull(configuredApp);

    Map<String, FuenteDeCopiaLocal> registeredSources = configuredApp.getFuentesRegistradas();

    assertEquals(2, registeredSources.size());
    assertTrue(registeredSources.containsKey("agregadora_principal"));
    assertTrue(registeredSources.containsKey("dinamica_principal"));
  }

  @Test
  @DisplayName("El método main se ejecuta sin lanzar excepciones (Test de Integración)")
  void mainSeEjecutaSinErrores() {
    assertDoesNotThrow(() -> App.main(new String[]{}),
        "El método main no debería lanzar una excepción al ejecutarse.");
  }
}
