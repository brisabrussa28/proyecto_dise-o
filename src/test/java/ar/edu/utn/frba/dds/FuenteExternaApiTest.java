package ar.edu.utn.frba.dds;

import ar.edu.utn.frba.dds.domain.fuentes.FuenteExternaAPI;
import ar.edu.utn.frba.dds.domain.fuentes.apis.FuenteAdapter;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.hecho.HechoBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Test de prueba del comportamiento de FuenteExternaAPI.
 */
@ExtendWith(MockitoExtension.class)
public class FuenteExternaApiTest {

  @Mock
  private FuenteAdapter adaptadorMock;

  private FuenteExternaAPI fuenteExterna;
  private Path tempJsonFilePath;

  @BeforeEach
  void setUp() throws Exception {
    tempJsonFilePath = Files.createTempFile("test_hechos_api_", ".json");
    fuenteExterna = new FuenteExternaAPI("Fuente Unificada Test", adaptadorMock, tempJsonFilePath.toString());
  }

  @AfterEach
  void tearDown() throws Exception {
    Files.deleteIfExists(tempJsonFilePath);
  }

  @Test
  @DisplayName("Obtiene y cachea una lista de hechos cuando el adaptador funciona")
  void obtieneYCacheaHechosExitosamente() throws IOException {
    Hecho hecho1 = new HechoBuilder()
        .conTitulo("Inundación grave")
        .conCategoria("inundacion")
        .conFechaSuceso(LocalDateTime.now().minusDays(1))
        .build();

    Hecho hecho2 = new HechoBuilder()
        .conTitulo("Terremoto leve")
        .conCategoria("terremoto")
        .conFechaSuceso(LocalDateTime.now().minusDays(2))
        .build();

    List<Hecho> hechosEsperados = List.of(hecho1, hecho2);

    when(adaptadorMock.consultarHechos()).thenReturn(hechosEsperados);

    fuenteExterna.forzarActualizacionSincrona();
    List<Hecho> hechosObtenidos = fuenteExterna.obtenerHechos();

    assertEquals(2, hechosObtenidos.size(), "Debería haber dos hechos en la lista.");
    assertTrue(hechosObtenidos.containsAll(hechosEsperados), "La lista obtenida debe contener todos los hechos esperados.");
  }

  @Test
  @DisplayName("Devuelve una lista vacía si el adaptador lanza una excepción")
  void devuelveListaVaciaCuandoAdaptadorFalla() throws IOException {
    when(adaptadorMock.consultarHechos()).thenThrow(new IOException("Simulando error de red"));

    fuenteExterna.forzarActualizacionSincrona();
    List<Hecho> hechosObtenidos = fuenteExterna.obtenerHechos();

    assertNotNull(hechosObtenidos, "La lista no debe ser nula.");
    assertTrue(hechosObtenidos.isEmpty(), "La lista de hechos debería estar vacía tras un error del adaptador.");
  }

  @Test
  @DisplayName("Maneja correctamente una respuesta vacía del adaptador")
  void manejaRespuestaVaciaDelAdaptador() throws IOException {
    when(adaptadorMock.consultarHechos()).thenReturn(Collections.emptyList());

    fuenteExterna.forzarActualizacionSincrona();
    List<Hecho> hechosObtenidos = fuenteExterna.obtenerHechos();

    assertNotNull(hechosObtenidos);
    assertTrue(hechosObtenidos.isEmpty(), "La lista de hechos debería estar vacía");
  }
}