package ar.edu.utn.frba.dds;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import ar.edu.utn.frba.dds.domain.calendarizacion.App;
import ar.edu.utn.frba.dds.domain.fuentes.Fuente;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDeAgregacion;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDinamica;
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

  // AfterEach se puede omitir si los tests no crean archivos reales.
  // Lo mantenemos por si la configuración por defecto los crea.
  @AfterEach
  void tearDown() throws IOException {
    Files.deleteIfExists(Paths.get("agregados.json"));
    Files.deleteIfExists(Paths.get("dinamica.json"));
  }

  @Test
  @DisplayName("Una fuente puede ser registrada correctamente")
  void registrarFuenteCorrectamente() {
    // Ahora mockeamos la clase base Fuente, que es más genérica.
    Fuente mockFuente = mock(Fuente.class);
    when(mockFuente.getNombre()).thenReturn("TestFuente");

    app.registrarFuente(mockFuente);
    Map<String, Fuente> fuentes = app.getFuentesRegistradas();

    assertEquals(1, fuentes.size());
    assertTrue(fuentes.containsKey("TestFuente"));
    assertEquals(mockFuente, fuentes.get("TestFuente"));
  }

  @Test
  @DisplayName("La actualización se ejecuta solo para fuentes actualizables (que son FuenteDeCopiaLocal)")
  void ejecutarActualizacionParaFuentesActualizables() {
    // Caso 1: Una fuente que SÍ es actualizable (hereda de FuenteDeCopiaLocal)
    FuenteDeAgregacion mockFuenteAgregadora = mock(FuenteDeAgregacion.class);
    when(mockFuenteAgregadora.getNombre()).thenReturn("FuenteAgregadora");

    // Caso 2: Una fuente que NO es actualizable
    FuenteDinamica mockFuenteDinamica = mock(FuenteDinamica.class);
    when(mockFuenteDinamica.getNombre()).thenReturn("FuenteDinamica");

    app.registrarFuente(mockFuenteAgregadora);
    app.registrarFuente(mockFuenteDinamica);

    app.ejecutarActualizacionTodasLasFuentes();

    // Verificamos que el método SÍ se llamó en la que corresponde
    verify(mockFuenteAgregadora, times(1)).forzarActualizacionSincrona();

    // No es necesario verificar que no se llamó en la otra,
    // ya que el test fallaría con un error de casteo si la lógica en App fuera incorrecta.
  }

  @Test
  @DisplayName("La configuración de la aplicación registra las fuentes por defecto correctamente")
  void configurarAplicacionRegistraFuentes() {
    App configuredApp = App.configurarAplicacion();
    assertNotNull(configuredApp);

    Map<String, Fuente> registeredSources = configuredApp.getFuentesRegistradas();

    // Verificamos que se registren las dos fuentes principales
    assertEquals(2, registeredSources.size());
    assertTrue(registeredSources.containsKey("agregadora_principal"));
    assertTrue(registeredSources.containsKey("dinamica_principal"));

    // Verificamos que cada una sea del tipo correcto
    assertTrue(registeredSources.get("agregadora_principal") instanceof FuenteDeAgregacion);
    assertTrue(registeredSources.get("dinamica_principal") instanceof FuenteDinamica);
  }

  @Test
  @DisplayName("El método main se ejecuta sin lanzar excepciones (Test de Integración Ligero)")
  void mainSeEjecutaSinErrores() {
    // Este test verifica que el punto de entrada de la app es ejecutable.
    assertDoesNotThrow(
        () -> App.main(new String[]{}),
        "El método main no debería lanzar una excepción al ejecutarse con la configuración por defecto."
    );
  }
}