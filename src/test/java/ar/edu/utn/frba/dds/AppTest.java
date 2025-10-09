package ar.edu.utn.frba.dds;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ar.edu.utn.frba.dds.domain.calendarizacion.App;
import ar.edu.utn.frba.dds.domain.fuentes.Fuente;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDeAgregacion;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteExternaAPI;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class AppTest {
  private App app;

  @BeforeEach
  void setUp() {
    app = new App();
  }

  @Test
  @DisplayName("Una fuente puede ser registrada correctamente")
  void registrarFuenteCorrectamente() {
    Fuente mockFuente = mock(Fuente.class);
    when(mockFuente.getNombre()).thenReturn("TestFuente");

    app.registrarFuente(mockFuente);
    Map<String, Fuente> fuentes = app.getFuentesRegistradas();

    assertEquals(1, fuentes.size());
    assertTrue(fuentes.containsKey("TestFuente"));
    assertEquals(mockFuente, fuentes.get("TestFuente"));
  }

  @Test
  @DisplayName("La actualización se ejecuta solo para fuentes que heredan de FuenteDeCopiaLocal")
  void ejecutarActualizacionParaFuentesCorrectas() {
    // Caso 1: Una fuente que SÍ es actualizable (hereda de FuenteDeCopiaLocal)
    FuenteExternaAPI mockFuenteActualizable = mock(FuenteExternaAPI.class);
    when(mockFuenteActualizable.getNombre()).thenReturn("FuenteAPI");

    // Caso 2: Una fuente que NO es actualizable (no hereda de FuenteDeCopiaLocal)
    FuenteDeAgregacion mockFuenteNoActualizable1 = mock(FuenteDeAgregacion.class);
    when(mockFuenteNoActualizable1.getNombre()).thenReturn("FuenteAgregadora");

    // Caso 3: Otra fuente que NO es actualizable
    FuenteDinamica mockFuenteNoActualizable2 = mock(FuenteDinamica.class);
    when(mockFuenteNoActualizable2.getNombre()).thenReturn("FuenteDinamica");

    app.registrarFuente(mockFuenteActualizable);
    app.registrarFuente(mockFuenteNoActualizable1);
    app.registrarFuente(mockFuenteNoActualizable2);

    app.ejecutarActualizacionTodasLasFuentes();

    // Verificamos que el método SÍ se llamó en la que corresponde
    verify(mockFuenteActualizable, times(1)).forzarActualizacionSincrona();

    // Verificamos explícitamente que el método NO se llamó en las otras.
    verify(mockFuenteNoActualizable1, never()).getHechos(); // Usamos un método diferente porque no tiene forzarActualizacion
    verify(mockFuenteNoActualizable2, never()).getHechos();
  }

  @Test
  @DisplayName("La configuración de la aplicación registra las fuentes por defecto correctamente")
  void configurarAplicacionRegistraFuentes() {
    App configuredApp = App.configurarAplicacion();
    assertNotNull(configuredApp);

    Map<String, Fuente> registeredSources = configuredApp.getFuentesRegistradas();

    assertEquals(2, registeredSources.size());
    assertTrue(registeredSources.containsKey("agregadora_principal"));
    assertTrue(registeredSources.containsKey("dinamica_principal"));

    assertTrue(registeredSources.get("agregadora_principal") instanceof FuenteDeAgregacion);
    assertTrue(registeredSources.get("dinamica_principal") instanceof FuenteDinamica);
  }

  @Test
  @DisplayName("El método main se ejecuta sin lanzar excepciones (Test de Integración Ligero)")
  void mainSeEjecutaSinErrores() {
    assertDoesNotThrow(
        () -> App.main(new String[]{}),
        "El método main no debería lanzar una excepción al ejecutarse con la configuración por defecto."
    );
  }
}
