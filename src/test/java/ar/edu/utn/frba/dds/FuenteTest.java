package ar.edu.utn.frba.dds;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ar.edu.utn.frba.dds.domain.fuentes.Fuente;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDeAgregacion;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteEstatica;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.serializadores.Serializador;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
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
    private Path tempFile;
    @Mock
    private Serializador<Hecho> serializadorMock;

    @BeforeEach
    void setUp() throws IOException {
      MockitoAnnotations.openMocks(this);
      tempFile = Files.createTempFile("test_fuente_dinamica_", ".json");
      when(serializadorMock.importar(anyString())).thenReturn(new ArrayList<>());
      fuente = new FuenteDinamica("MiFuente", tempFile.toString(), serializadorMock);
    }

    @AfterEach
    void tearDown() throws IOException {
      Files.deleteIfExists(tempFile);
    }

    @Test
    public void iniciaConListaVaciaSiNoHayCache() {
      assertTrue(fuente.obtenerHechos().isEmpty());
      verify(serializadorMock).importar(tempFile.toString());
    }

    @Test
    public void agregaHechoYLoPersiste() {
      Hecho hechoMock = mock(Hecho.class);
      fuente.agregarHecho(hechoMock);

      assertEquals(1, fuente.obtenerHechos().size());
      assertEquals(hechoMock, fuente.obtenerHechos().get(0));
      verify(serializadorMock).exportar(anyList(), eq(tempFile.toString()));
    }

    @Test
    public void obtenerHechosDevuelveCopiaInmutable() {
      fuente.agregarHecho(mock(Hecho.class));
      List<Hecho> hechos = fuente.obtenerHechos();
      assertThrows(UnsupportedOperationException.class, () -> hechos.add(mock(Hecho.class)));
    }
  }

  @Nested
  @DisplayName("Pruebas para FuenteEstatica")
  class FuenteEstaticaTests {
    @Test
    @SuppressWarnings("unchecked")
    public void usaSerializadorParaCargar() {
      Hecho hechoMock = mock(Hecho.class);
      Serializador<Hecho> serializadorMock = mock(Serializador.class);
      when(serializadorMock.importar("ruta.csv")).thenReturn(List.of(hechoMock));
      FuenteEstatica fuente = new FuenteEstatica("MiFuente", "ruta.csv", serializadorMock);

      List<Hecho> hechos = fuente.obtenerHechos();

      assertEquals(1, hechos.size());
      assertEquals(hechoMock, hechos.get(0));
      verify(serializadorMock).importar("ruta.csv");
    }
  }

  @Nested
  @DisplayName("Pruebas para FuenteDeAgregacion")
  class FuenteDeAgregacionTests {
    private FuenteDeAgregacion agregadora;
    private Path tempFile;
    @Mock
    private Serializador<Hecho> serializadorMock;
    @Mock
    private Fuente fuenteMock1;
    @Mock
    private Fuente fuenteMock2;
    @Mock
    private Hecho hechoMock1;
    @Mock
    private Hecho hechoMock2;

    @BeforeEach
    void setUp() throws IOException {
      MockitoAnnotations.openMocks(this);
      tempFile = Files.createTempFile("test_agregacion_", ".json");
      when(serializadorMock.importar(anyString())).thenReturn(new ArrayList<>());
      agregadora = new FuenteDeAgregacion("TestAgregadora", tempFile.toString(), serializadorMock);
    }

    @AfterEach
    void tearDown() throws IOException {
      Files.deleteIfExists(tempFile);
    }

    @Test
    @DisplayName("Debe iniciar con una lista vacía y intentar cargar la caché")
    public void iniciaVaciaSiNoHayCache() {
      assertTrue(agregadora.obtenerHechos().isEmpty());
      verify(serializadorMock).importar(tempFile.toString());
    }

    @Test
    @DisplayName("Debe combinar hechos de múltiples fuentes y persistir el resultado")
    public void combinaHechosYPersiste() {
      when(fuenteMock1.obtenerHechos()).thenReturn(List.of(hechoMock1));
      when(fuenteMock2.obtenerHechos()).thenReturn(List.of(hechoMock2));
      agregadora.agregarFuente(fuenteMock1);
      agregadora.agregarFuente(fuenteMock2);

      agregadora.forzarActualizacionSincrona();
      List<Hecho> todos = agregadora.obtenerHechos();

      assertEquals(2, todos.size());
      assertTrue(todos.containsAll(List.of(hechoMock1, hechoMock2)));
      verify(serializadorMock).exportar(todos, tempFile.toString());
    }
  }
}

