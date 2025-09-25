package ar.edu.utn.frba.dds;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ar.edu.utn.frba.dds.domain.exportador.Exportador;
import ar.edu.utn.frba.dds.domain.exportador.configuracion.ConfiguracionExportador;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteExternaAPI;
import ar.edu.utn.frba.dds.domain.fuentes.apis.FuenteAdapter;
import ar.edu.utn.frba.dds.domain.fuentes.apis.configuracion.ConfiguracionAdapter;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.hecho.HechoBuilder;
import ar.edu.utn.frba.dds.domain.lector.Lector;
import ar.edu.utn.frba.dds.domain.lector.configuracion.ConfiguracionLector;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class FuenteExternaApiTest {

  @Mock private FuenteAdapter adaptadorMock;
  @Mock private Lector<Hecho> lectorMock;
  @Mock private Exportador<Hecho> exportadorMock;
  @Mock private ConfiguracionAdapter configAdapterMock;
  @Mock private ConfiguracionLector configLectorMock;
  @Mock private ConfiguracionExportador configExportadorMock;

  private FuenteExternaAPI fuenteExterna;
  private final String RUTA_COPIA_LOCAL = "copia_api_test.json";

  @BeforeEach
  void setUp() {
    // CORRECCIÓN: Usar doReturn(...).when(...) para todos los mocks de build()
    doReturn(adaptadorMock).when(configAdapterMock).build();
    doReturn(lectorMock).when(configLectorMock).build(Hecho.class);
    doReturn(exportadorMock).when(configExportadorMock).build();

    when(lectorMock.importar(RUTA_COPIA_LOCAL)).thenReturn(new ArrayList<>());

    fuenteExterna = new FuenteExternaAPI(
        "FuenteAPITest",
        configAdapterMock,
        RUTA_COPIA_LOCAL,
        configLectorMock,
        configExportadorMock
    );
  }

  @Test
  @DisplayName("Obtiene y cachea una lista de hechos cuando el adaptador funciona")
  void obtieneYCacheaHechosExitosamente() throws IOException {
    Hecho hecho = new HechoBuilder().conTitulo("Test").conFechaSuceso(LocalDateTime.now().minusWeeks(3)).build();
    List<Hecho> hechosEsperados = List.of(hecho);
    when(adaptadorMock.consultarHechos()).thenReturn(hechosEsperados);

    fuenteExterna.forzarActualizacionSincrona();
    List<Hecho> hechosObtenidos = fuenteExterna.obtenerHechos();

    assertEquals(1, hechosObtenidos.size());
    assertTrue(hechosObtenidos.contains(hecho));
    verify(exportadorMock).exportar(hechosEsperados, RUTA_COPIA_LOCAL);
  }

  @Test
  @DisplayName("No exporta si el adaptador falla")
  void noExportaSiAdaptadorFalla() throws IOException {
    when(adaptadorMock.consultarHechos()).thenThrow(new IOException("Simulando error de red"));
    fuenteExterna.forzarActualizacionSincrona();
    verify(exportadorMock, never()).exportar(anyList(), anyString());
  }
}