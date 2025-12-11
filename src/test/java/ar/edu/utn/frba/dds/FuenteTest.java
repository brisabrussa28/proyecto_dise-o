package ar.edu.utn.frba.dds;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ar.edu.utn.frba.dds.model.fuentes.Fuente;
import ar.edu.utn.frba.dds.model.fuentes.FuenteDeAgregacion;
import ar.edu.utn.frba.dds.model.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.model.hecho.Hecho;
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
    @DisplayName("Una fuente dinámica inicia con una lista de hechos vacía")
    public void iniciaConListaVacia() {
      assertTrue(fuente.getHechos().isEmpty());
    }

    @Test
    @DisplayName("Se puede agregar un hecho correctamente a una fuente dinámica")
    public void agregaHechoCorrectamente() {
      Hecho hechoMock = mock(Hecho.class);
      fuente.agregarHecho(hechoMock);
      assertEquals(1, fuente.getHechos().size());
      assertEquals(hechoMock, fuente.getHechos().get(0));
    }
  }

//  @Nested
//  @DisplayName("Pruebas para FuenteEstatica")
//  class FuenteEstaticaTests {
//    @Test
//    @DisplayName("Usa la configuración del lector para cargar los hechos del archivo")
//    public void usaConfiguracionDeLectorParaCargar() {
//      Hecho hechoMock = mock(Hecho.class);
//      Lector<Hecho> lectorMock = mock(Lector.class);
//      ConfiguracionLector configLectorMock = mock(ConfiguracionLector.class);
//
//      when(configLectorMock.build(Hecho.class)).thenReturn(lectorMock);
//      when(lectorMock.importar("ruta.csv")).thenReturn(List.of(hechoMock));
//
//      FuenteEstatica fuente = new FuenteEstatica("MiFuente", "ruta.csv", configLectorMock);
//      List<Hecho> hechos = fuente.getHechos();
//
//      assertEquals(1, hechos.size());
//      assertEquals(hechoMock, hechos.get(0));
//      verify(lectorMock).importar("ruta.csv");
//    }
//  }

  @Nested
  @DisplayName("Pruebas para FuenteDeAgregacion")
  class FuenteDeAgregacionTests {
    private FuenteDeAgregacion agregadora;
    @Mock private Fuente fuenteMock1;
    @Mock private Fuente fuenteMock2;
    @Mock private Hecho hechoMock1;
    @Mock private Hecho hechoMock2;
    @Mock private Hecho hechoComun;

    @BeforeEach
    void setUp() {
      MockitoAnnotations.openMocks(this);
      agregadora = new FuenteDeAgregacion("TestAgregadora");
    }

    @Test
    @DisplayName("Debe combinar hechos de múltiples fuentes en tiempo real y sin duplicados")
    public void combinaHechosEnTiempoReal() {
      // Configuración de los mocks
      when(fuenteMock1.getHechos()).thenReturn(List.of(hechoMock1, hechoComun));
      when(fuenteMock2.getHechos()).thenReturn(List.of(hechoMock2, hechoComun));
      agregadora.agregarFuente(fuenteMock1);
      agregadora.agregarFuente(fuenteMock2);

      // Ejecución
      List<Hecho> todos = agregadora.getHechos();

      // Verificación
      assertEquals(3, todos.size()); // 2 hechos únicos + 1 común
      assertTrue(todos.containsAll(List.of(hechoMock1, hechoMock2, hechoComun)));
    }
  }
}
