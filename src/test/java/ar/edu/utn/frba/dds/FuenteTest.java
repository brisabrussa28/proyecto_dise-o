package ar.edu.utn.frba.dds;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import ar.edu.utn.frba.dds.domain.exportador.Exportador;
import ar.edu.utn.frba.dds.domain.exportador.configuracion.ConfiguracionExportador;
import ar.edu.utn.frba.dds.domain.fuentes.Fuente;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDeAgregacion;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteEstatica;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.lector.Lector;
import ar.edu.utn.frba.dds.domain.lector.configuracion.ConfiguracionLector;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class FuenteTest {

  @Nested
  @DisplayName("Pruebas para FuenteDinamica")
  class FuenteDinamicaTests {
    private FuenteDinamica fuente;

    @BeforeEach
    void setUp() {
      fuente = new FuenteDinamica("MiFuenteDinamica");
    }

    @Test
    public void iniciaConListaVacia() {
      assertTrue(fuente.obtenerHechos().isEmpty());
    }

    @Test
    public void agregaHechoCorrectamente() {
      Hecho hechoMock = mock(Hecho.class);
      fuente.agregarHecho(hechoMock);
      assertEquals(1, fuente.obtenerHechos().size());
      assertEquals(hechoMock, fuente.obtenerHechos().get(0));
    }
  }

  @Nested
  @DisplayName("Pruebas para FuenteEstatica")
  class FuenteEstaticaTests {
    @Test
    public void usaConfiguracionDeLectorParaCargar() {
      Hecho hechoMock = mock(Hecho.class);
      Lector<Hecho> lectorMock = mock(Lector.class);
      ConfiguracionLector configLectorMock = mock(ConfiguracionLector.class);

      // CORRECCIÓN: Usar doReturn(...).when(...)
      doReturn(lectorMock).when(configLectorMock).build(Hecho.class);
      when(lectorMock.importar("ruta.csv")).thenReturn(List.of(hechoMock));

      FuenteEstatica fuente = new FuenteEstatica("MiFuente", "ruta.csv", configLectorMock);
      List<Hecho> hechos = fuente.obtenerHechos();

      assertEquals(1, hechos.size());
      assertEquals(hechoMock, hechos.get(0));
      verify(lectorMock).importar("ruta.csv");
    }
  }

  @Nested
  @DisplayName("Pruebas para FuenteDeAgregacion")
  class FuenteDeAgregacionTests {
    private FuenteDeAgregacion agregadora;
    private final String RUTA_COPIA = "test_agregacion.json";
    @Mock private Lector<Hecho> lectorMock;
    @Mock private Exportador<Hecho> exportadorMock;
    @Mock private ConfiguracionLector configLectorMock;
    @Mock private ConfiguracionExportador configExportadorMock;
    @Mock private Fuente fuenteMock1;
    @Mock private Fuente fuenteMock2;
    @Mock private Hecho hechoMock1;
    @Mock private Hecho hechoMock2;

    @BeforeEach
    void setUp() {
      MockitoAnnotations.openMocks(this);

      // CORRECCIÓN: Usar doReturn(...).when(...)
      doReturn(lectorMock).when(configLectorMock).build(Hecho.class);
      doReturn(exportadorMock).when(configExportadorMock).build();
      when(lectorMock.importar(anyString())).thenReturn(new ArrayList<>());

      agregadora = new FuenteDeAgregacion("TestAgregadora", RUTA_COPIA, configLectorMock, configExportadorMock);
    }

    @Test
    @DisplayName("Debe combinar hechos de múltiples fuentes y persistir")
    public void combinaHechosYPersiste() {
      when(fuenteMock1.obtenerHechos()).thenReturn(List.of(hechoMock1));
      when(fuenteMock2.obtenerHechos()).thenReturn(List.of(hechoMock2));
      agregadora.agregarFuente(fuenteMock1);
      agregadora.agregarFuente(fuenteMock2);

      agregadora.forzarActualizacionSincrona();
      List<Hecho> todos = agregadora.obtenerHechos();

      assertEquals(2, todos.size());
      assertTrue(todos.containsAll(List.of(hechoMock1, hechoMock2)));
      verify(exportadorMock).exportar(todos, RUTA_COPIA);
    }
  }
}