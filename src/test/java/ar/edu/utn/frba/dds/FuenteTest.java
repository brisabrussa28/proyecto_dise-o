package ar.edu.utn.frba.dds;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import ar.edu.utn.frba.dds.domain.exportador.Exportador;
import ar.edu.utn.frba.dds.domain.exportador.configuracion.ConfiguracionExportador;
import ar.edu.utn.frba.dds.domain.fuentes.Fuente;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDeAgregacion;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteEstatica;
import ar.edu.utn.frba.dds.domain.geilocalizacion.ServicioGeoref;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.lector.Lector;
import ar.edu.utn.frba.dds.domain.lector.configuracion.ConfiguracionLector;
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

  @Nested
  @DisplayName("Pruebas para completar provincias en Hechos")
  class CompletarProvinciasTests {
    @Mock
    private ServicioGeoref servicioGeorefMock;

    private Fuente fuente;

    @BeforeEach
    void setUp() {
      MockitoAnnotations.openMocks(this);

      // Fuente "anónima" para el test
      fuente = new Fuente("FuenteTest") {
        private final List<Hecho> hechos = new ArrayList<>();
        @Override
        public List<Hecho> obtenerHechos() {
          return hechos;
        }
        public void agregarHecho(Hecho h) { hechos.add(h); }
      };
    }

    @Test
    @DisplayName("Debe completar la provincia de un hecho con provincia nula")
    void completaProvinciaSiFalta() {
      PuntoGeografico ubicacion = new PuntoGeografico(-27.2741, -66.7529);
      Hecho hecho = new Hecho();
      hecho.setUbicacion(ubicacion);
      hecho.setProvincia(null);

      fuente.obtenerHechos().add(hecho);

      when(servicioGeorefMock.obtenerProvincia(-27.2741, -66.7529))
          .thenReturn("Catamarca");

      for (Hecho h : fuente.obtenerHechos()) {
        if (h.getProvincia() == null || h.getProvincia().isBlank()) {
          String provincia = servicioGeorefMock.obtenerProvincia(
              h.getUbicacion().getLatitud(),
              h.getUbicacion().getLongitud()
          );
          h.setProvincia(provincia);
          // System.out.println("Provincia obtenida desde el mock: " + provincia);
        }
      }

      assertEquals("Catamarca", hecho.getProvincia());
      verify(servicioGeorefMock).obtenerProvincia(-27.2741, -66.7529);
    }

    @Test
    @DisplayName("Debe obtener la provincia real desde la API de Georef")
    void obtieneProvinciaReal() {
      ServicioGeoref servicio = new ServicioGeoref();

      double lat = -27.2741;
      double lon = -66.7529;

      String provincia = servicio.obtenerProvincia(lat, lon);
      // System.out.println("Provincia obtenida desde la API real: " + provincia);

      // Sabemos que esas coordenadas son Catamarca
      assertEquals("Catamarca", provincia);
    }

    @Test
    @DisplayName("No debe modificar hechos que ya tienen provincia")
    void noModificaProvinciaExistente() {
      Hecho hecho = new Hecho();
      hecho.setProvincia("Buenos Aires");
      hecho.setUbicacion(new PuntoGeografico(-34.6, -58.38));

      fuente.obtenerHechos().add(hecho);

      // Aunque el mock devuelva otra provincia, no debería usarse
      when(servicioGeorefMock.obtenerProvincia(anyDouble(), anyDouble()))
          .thenReturn("Catamarca");

      for (Hecho h : fuente.obtenerHechos()) {
        if (h.getProvincia() == null || h.getProvincia().isBlank()) {
          String provincia = servicioGeorefMock.obtenerProvincia(
              h.getUbicacion().getLatitud(),
              h.getUbicacion().getLongitud()
          );
          h.setProvincia(provincia);
        }
      }

      assertEquals("Buenos Aires", hecho.getProvincia());
      verify(servicioGeorefMock, never()).obtenerProvincia(anyDouble(), anyDouble());
    }

  }

}