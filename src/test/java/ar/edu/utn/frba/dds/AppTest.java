package ar.edu.utn.frba.dds;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ar.edu.utn.frba.dds.domain.calendarizacion.App;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteCacheable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class AppTest {

  private App app;
  private FuenteCacheable mockFuente;
  private final String testFuenteName = "TestFuente";
  private static final Path COPIAS_DIR = Paths.get("copias");

  @BeforeEach
  void setUp() {
    app = new App();
    mockFuente = mock(FuenteCacheable.class);
    when(mockFuente.getNombre()).thenReturn(testFuenteName);

    try {
      Files.createDirectories(COPIAS_DIR);
    } catch (IOException e) {
      throw new RuntimeException("No se pudo crear el directorio de copias para el test", e);
    }
  }

  @AfterEach
  void tearDown() {
    // FIX: Limpiar el directorio 'copias' y su contenido después de cada test
    // para asegurar que los tests sean independientes.
    try {
      if (Files.exists(COPIAS_DIR)) {
        Files.walk(COPIAS_DIR)
            .sorted(Comparator.reverseOrder())
            .forEach(path -> {
              try {
                Files.delete(path);
              } catch (IOException e) {
                System.err.println("No se pudo eliminar el archivo de test: " + path);
              }
            });
      }
    } catch (IOException e) {
      System.err.println("No se pudo limpiar el directorio de test: " + COPIAS_DIR);
    }
  }

  @Test
  @DisplayName("Una fuente puede ser registrada correctamente")
  void registrarFuenteCorrectamente() {
    app.registrarFuente(mockFuente);
    Map<String, FuenteCacheable> fuentes = app.getFuentesRegistradas();

    assertNotNull(fuentes, "El mapa de fuentes no debería ser nulo.");
    assertEquals(1, fuentes.size(), "El tamaño del mapa debería ser 1.");
    assertTrue(
        fuentes.containsKey(testFuenteName),
        "El mapa debería contener la fuente registrada con su nombre."
    );
    assertEquals(
        mockFuente,
        fuentes.get(testFuenteName),
        "El objeto fuente en el mapa debe ser el mismo que se registró."
    );
  }

  @Test
  @DisplayName("Registrar una fuente nula no debe agregarla ni lanzar excepción")
  void registrarFuenteNula() {
    app.registrarFuente(null);
    Map<String, FuenteCacheable> fuentes = app.getFuentesRegistradas();

    assertNotNull(fuentes, "El mapa de fuentes no debería ser nulo.");
    assertTrue(fuentes.isEmpty(), "El mapa de fuentes debería estar vacío.");
  }

  @Test
  @DisplayName("La actualización se ejecuta para una fuente registrada")
  void ejecutarActualizacionParaFuenteRegistrada() {
    app.registrarFuente(mockFuente);
    app.ejecutarActualizacion(testFuenteName);

    verify(mockFuente, times(1)).forzarActualizacionSincrona();
  }


  @Test
  @DisplayName("El método main lanza IllegalStateException si no se proveen argumentos")
  void mainLanzaExcepcionSinArgumentos() {
    assertThrows(IllegalStateException.class, () -> App.main(new String[]{}));
  }

  @Test
  @DisplayName("La configuración de la aplicación registra las fuentes por defecto")
  void configurarAplicacionRegistraFuentes() {
    App configuredApp = App.configurarAplicacion();
    assertNotNull(configuredApp, "La aplicación configurada no debería ser nula.");

    Map<String, FuenteCacheable> registeredSources = configuredApp.getFuentesRegistradas();

    assertEquals(2, registeredSources.size(), "Deberían registrarse 2 fuentes por defecto.");
    assertTrue(
        registeredSources.containsKey("agregadora_principal"),
        "Debería contener 'agregadora_principal'."
    );
    //assertTrue(registeredSources.containsKey("fuente_externa_demo"), "Debería contener 'fuente_externa_demo'.");
  }

  @Test
  @DisplayName("El método main ejecuta la actualización para una fuente válida (Test de Integración)")
  void mainEjecutaActualizacion() {
    // Este test de integración verifica el flujo principal con una fuente basada en archivos.
    assertDoesNotThrow(
        () -> App.main(new String[]{"agregadora_principal"}),
        "El método main no debería lanzar una excepción para 'agregadora_principal'."
    );
  }
}
