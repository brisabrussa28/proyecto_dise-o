package ar.edu.utn.frba.dds;

import ar.edu.utn.frba.dds.domain.fuentes.FuenteExternaAPI;
import ar.edu.utn.frba.dds.domain.fuentes.apis.FuenteAdapter;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.hecho.HechoBuilder;
import ar.edu.utn.frba.dds.domain.serializadores.Lector.Lector;
import ar.edu.utn.frba.dds.domain.serializadores.exportador.Exportador;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas del comportamiento de FuenteExternaAPI con la nueva arquitectura de serializadores.
 */
@ExtendWith(MockitoExtension.class)
public class FuenteExternaApiTest {

  @Mock
  private FuenteAdapter adaptadorMock;
  @Mock
  private Lector<Hecho> lectorMock;
  @Mock
  private Exportador<Hecho> exportadorMock;

  private FuenteExternaAPI fuenteExterna;
  private final String RUTA_COPIA_LOCAL = "copia_api_test.json";

  @BeforeEach
  void setUp() {
    when(lectorMock.importar(RUTA_COPIA_LOCAL)).thenReturn(new ArrayList<>());

    fuenteExterna = new FuenteExternaAPI("FuenteAPITest", adaptadorMock, RUTA_COPIA_LOCAL, lectorMock, exportadorMock);
  }

  @Test
  @DisplayName("Obtiene y cachea una lista de hechos cuando el adaptador funciona")
  void obtieneYCacheaHechosExitosamente() throws IOException {
    Hecho hecho = new HechoBuilder().conTitulo("Test")
                                    .conFechaSuceso(LocalDateTime.now()
                                                                 .minusDays(1))
                                    .build();
    List<Hecho> hechosEsperados = List.of(hecho);
    when(adaptadorMock.consultarHechos()).thenReturn(hechosEsperados);

    fuenteExterna.forzarActualizacionSincrona();
    List<Hecho> hechosObtenidos = fuenteExterna.obtenerHechos();

    assertEquals(1, hechosObtenidos.size());
    assertTrue(hechosObtenidos.contains(hecho));
    verify(exportadorMock).exportar(hechosEsperados, RUTA_COPIA_LOCAL);
  }

  @Test
  @DisplayName("Persiste una lista vacía si el adaptador lanza una excepción")
  void persisteListaVaciaCuandoAdaptadorFalla() throws IOException {
    when(adaptadorMock.consultarHechos()).thenThrow(new IOException("Simulando error de red"));

    fuenteExterna.forzarActualizacionSincrona();
    List<Hecho> hechosObtenidos = fuenteExterna.obtenerHechos();

    assertTrue(hechosObtenidos.isEmpty(), "La lista de hechos debería estar vacía.");
    verify(exportadorMock).exportar(eq(Collections.emptyList()), eq(RUTA_COPIA_LOCAL));
  }

  @Test
  @DisplayName("Maneja y persiste una respuesta vacía del adaptador")
  void manejaRespuestaVaciaDelAdaptador() throws IOException {
    when(adaptadorMock.consultarHechos()).thenReturn(Collections.emptyList());

    fuenteExterna.forzarActualizacionSincrona();
    List<Hecho> hechosObtenidos = fuenteExterna.obtenerHechos();

    assertTrue(hechosObtenidos.isEmpty());
    verify(exportadorMock).exportar(Collections.emptyList(), RUTA_COPIA_LOCAL);
  }
}
