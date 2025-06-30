package ar.edu.utn.frba.dds;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import ar.edu.utn.frba.dds.domain.exceptions.ConexionFuenteDemoException;
import ar.edu.utn.frba.dds.domain.fuentes.Conexion;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDemo;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.net.URL;
import java.nio.file.Files; // Import para manejar archivos temporales
import java.nio.file.Path;  // Import para Path
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach; // Import para limpieza después de cada test
import org.junit.jupiter.api.BeforeEach;
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
  private Path tempJsonFilePath; // Para almacenar la ruta al archivo JSON temporal

  @BeforeEach
  void setUp() throws Exception {
    dummyUrl = new URL("http://localhost:8080/hechos");
    // Crear un archivo temporal para la ruta del JSON. Esto asegura que cada test
    // tenga un archivo limpio y no interfiera con otros tests.
    tempJsonFilePath = Files.createTempFile("test_hechos", ".json");
    // Inicializar FuenteDemo con la URL dummy, la conexión mock y la ruta del archivo JSON temporal
    fuenteDemo = new FuenteDemo(dummyUrl, conexionMock, tempJsonFilePath.toString());
  }

  @AfterEach
  void tearDown() throws Exception {
    // Limpiar el archivo JSON temporal después de cada test
    Files.deleteIfExists(tempJsonFilePath);
    // Detener el scheduler en FuenteDemo para evitar que tareas en segundo plano
    // interfieran con otros tests o mantengan el hilo de la JVM activo.
    fuenteDemo.detenerScheduler();
  }

  @Test
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

    // Configurar el mock para devolver los hechos en secuencia
    when(conexionMock.siguienteHecho(any(), any()))
        .thenReturn(hecho1)
        .thenReturn(hecho2)
        .thenReturn(null);

    // Llamar al método actualizado en FuenteDemo
    fuenteDemo.actualizarHechosYGuardarCopia();

    // Verificar que los hechos se hayan actualizado correctamente en la caché de FuenteDemo
    List<Hecho> hechos = fuenteDemo.obtenerHechos();
    assertEquals(2, hechos.size());
    assertEquals(
        "Incendio A",
        hechos.get(0)
            .getTitulo()
    );
    assertEquals(
        "Incendio B",
        hechos.get(1)
            .getTitulo()
    );
  }

  @Test
  void seProduceErrorSiLosDatosSonInvalidos() {
    Map<String, Object> hechoInvalido = Map.of(
        "titulo", "Hecho inválido",
        "descripcion", "Falla en datos",
        "categoria", "Error",
        "direccion", "N/A",
        // Falta "ubicacion" intencionalmente para causar un error de cast o NPE
        "fechaSuceso", "2024-05-01T10:00:00",
        "fechaCarga", "2024-06-01T10:00:00",
        "fuenteOrigen", "FUENTE_DEMO",
        "etiquetas", List.of("error")
    );

    // Configurar el mock para devolver el hecho inválido
    when(conexionMock.siguienteHecho(any(), any()))
        .thenReturn(hechoInvalido)
        .thenReturn(null);

    // Esperar una ConexionFuenteDemoException cuando se llama al método actualizado
    Exception exception = assertThrows(ConexionFuenteDemoException.class, () -> {
      fuenteDemo.actualizarHechosYGuardarCopia();
    });

    // Verificar el tipo y el mensaje de la excepción
    assertInstanceOf(ConexionFuenteDemoException.class, exception);
    assertTrue(exception.getMessage().contains("Error al consultar FuenteDemo"));
  }
}
