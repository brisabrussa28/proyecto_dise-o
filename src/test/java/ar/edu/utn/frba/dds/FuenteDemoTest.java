package ar.edu.utn.frba.dds;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import ar.edu.utn.frba.dds.domain.Conexion.Conexion;
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
    fuenteDemo = new FuenteDemo("Fuente Demo Test", dummyUrl, conexionMock, tempJsonFilePath.toString());

    // Configuración por defecto para que los tests que no dependen de la respuesta no fallen.
    when(conexionMock.siguienteHecho(any(), any())).thenReturn(null);
  }

  @AfterEach
  void tearDown() throws Exception {
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
        "provincia", "Buenos Aires",
        "ubicacion", Map.of("latitud", -34.0, "longitud", -58.0),
        "fechaSuceso", "2024-05-01T10:00:00",
        "fechaCarga", "2024-06-01T10:00:00",
        "fuenteOrigen", "DATASET",
        "etiquetas", List.of("fuego", "emergencia")
    );
    Map<String, Object> hecho2 = Map.of(
        "titulo", "Incendio B",
        "descripcion", "Otro incendio.",
        "categoria", "Incendios",
        "direccion", "Ruta B",
        "provincia", "Córdoba",
        "ubicacion", Map.of("latitud", -32.0, "longitud", -65.0),
        "fechaSuceso", "2024-05-02T15:30:00",
        "fechaCarga", "2024-06-02T12:00:00",
        "fuenteOrigen", "DATASET",
        "etiquetas", List.of("fuego", "alerta")
    );

    when(conexionMock.siguienteHecho(any(), any()))
        .thenReturn(hecho1)
        .thenReturn(hecho2)
        .thenReturn(null);

    fuenteDemo.forzarActualizacionSincrona();

    List<Hecho> hechos = fuenteDemo.obtenerHechos();
    assertEquals(2, hechos.size());
    assertEquals("Incendio A", hechos.get(0).getTitulo());
    assertEquals("Incendio B", hechos.get(1).getTitulo());
  }

  @Test
  @DisplayName("No debe actualizar la caché si la fuente externa devuelve datos inválidos")
  void noSeActualizaLaCacheSiLosDatosSonInvalidos() {
    fuenteDemo.forzarActualizacionSincrona();
    assertTrue(fuenteDemo.obtenerHechos().isEmpty(), "La caché debe estar vacía al inicio");

    Map<String, Object> hechoInvalido = Map.of(
        "titulo", "Hecho inválido",
        "descripcion", "Falla en datos"
        // Faltan categoria y fechaSuceso, que son obligatorios para el HechoBuilder
    );

    when(conexionMock.siguienteHecho(any(), any()))
        .thenReturn(hechoInvalido)
        .thenReturn(null);

    // Con el nuevo código resiliente, la actualización no debería lanzar una excepción.
    assertDoesNotThrow(() -> fuenteDemo.forzarActualizacionSincrona(),
        "El proceso de actualización no debe fallar por un único hecho inválido.");

    // La caché debe seguir vacía porque el único hecho proporcionado era inválido y fue omitido.
    assertTrue(fuenteDemo.obtenerHechos().isEmpty(), "La caché debe permanecer vacía después de intentar procesar datos inválidos.");
  }
}

