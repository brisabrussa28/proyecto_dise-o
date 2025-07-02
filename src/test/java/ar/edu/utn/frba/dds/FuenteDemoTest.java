package ar.edu.utn.frba.dds;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import ar.edu.utn.frba.dds.domain.fuentes.Conexion;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDemo;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class FuenteDemoTest {
  @Mock
  Conexion conexionMock;

  FuenteDemo fuenteDemo;
  URL dummyUrl;
  private Path tempJsonFilePath;

  @BeforeEach
  void setUp() throws Exception {
    dummyUrl = new URL("http://localhost:8080/hechos");
    tempJsonFilePath = Files.createTempFile("test_hechos_", ".json");
    // FIX: Se agrega el parámetro 'nombre' faltante en el constructor.
    fuenteDemo = new FuenteDemo("TestDemo", dummyUrl, conexionMock, tempJsonFilePath.toString());
  }

  @AfterEach
  void tearDown() throws Exception {
    // Es una buena práctica detener el scheduler para no dejar hilos corriendo.
    fuenteDemo.detenerScheduler();
    Files.deleteIfExists(tempJsonFilePath);
  }

  @Test
  @DisplayName("Debe actualizar la caché con hechos válidos de la fuente externa")
  void seActualizanHechosDesdeFuenteExterna() {
    Map<String, Object> hecho1 = Map.of(
        "titulo", "Incendio A",
        "descripcion", "Un incendio en la zona sur.",
        "categoria", "Incendios",
        "direccion", "Calle Falsa 123",
        "ubicacion", Map.of("latitud", -34.0, "longitud", -58.0),
        "fechaSuceso", "2024-05-01T10:00:00",
        "fechaCarga", "2024-06-01T10:00:00",
        "fuenteOrigen", "FUENTE_DEMO",
        "etiquetas", List.of("fuego", "emergencia")
    );
    Map<String, Object> hecho2 = Map.of(
        "titulo", "Incendio B",
        "descripcion", "Otro incendio.",
        "categoria", "Incendios",
        "direccion", "Ruta B",
        "ubicacion", Map.of("latitud", -32.0, "longitud", -65.0),
        "fechaSuceso", "2024-05-02T15:30:00",
        "fechaCarga", "2024-06-02T12:00:00",
        "fuenteOrigen", "FUENTE_DEMO",
        "etiquetas", List.of("fuego", "alerta")
    );

    when(conexionMock.siguienteHecho(any(), any()))
        .thenReturn(hecho1)
        .thenReturn(hecho2)
        .thenReturn(null);

    // ACT: Llamamos al método público para forzar la actualización síncrona.
    fuenteDemo.forzarActualizacionSincrona();

    // ASSERT: Verificamos que la caché interna se haya actualizado.
    List<Hecho> hechos = fuenteDemo.obtenerHechos();
    assertEquals(2, hechos.size());
    assertEquals("Incendio A", hechos.get(0).getTitulo());
    assertEquals("Incendio B", hechos.get(1).getTitulo());
  }

  @Test
  @DisplayName("No debe actualizar la caché si la fuente externa devuelve datos inválidos")
  void noSeActualizaLaCacheSiLosDatosSonInvalidos() {
    Map<String, Object> hechoInvalido = Map.of(
        "titulo", "Hecho inválido",
        "descripcion", "Falla en datos",
        // Falta "ubicacion" y otros campos para causar un error de parseo.
        "fechaSuceso", "2024-05-01T10:00:00"
    );

    when(conexionMock.siguienteHecho(any(), any()))
        .thenReturn(hechoInvalido)
        .thenReturn(null);

    // ACT: La llamada a la actualización ahora captura la excepción internamente.
    fuenteDemo.forzarActualizacionSincrona();

    // ASSERT: Verificamos que la caché siga vacía, ya que la actualización falló.
    assertTrue(
        fuenteDemo.obtenerHechos().isEmpty(),
        "La caché debería estar vacía si la actualización falla por datos inválidos."
    );
  }
}
