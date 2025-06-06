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
import java.util.List;
import java.util.Map;
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

  @BeforeEach
  void setUp() throws Exception {
    dummyUrl = new URL("http://localhost:8080/hechos");
    fuenteDemo = new FuenteDemo(dummyUrl, conexionMock);
    fuenteDemo.detenerScheduler(); // Detenemos proximas ejecuciones automáticas
  }

  @Test
  void seObtieneUnHechoYLoGuardaEnCache() {
    // mockear un hecho
    Map<String, Object> fakeHecho = Map.of(
        "titulo", "Incendio en zona rural",
        "descripcion", "Se reportó un incendio forestal cerca del parque.",
        "categoria", "Incendios",
        "direccion", "Ruta 12, km 45",
        "ubicacion", Map.of("latitud", -31.4167, "longitud", -64.1833),
        "fechaSuceso", "2024-05-01T15:30:00",
        "fechaCarga", "2024-06-01T12:00:00",
        "fuenteOrigen", "FUENTE_DEMO",
        "etiquetas", List.of("fuego", "emergencia", "forestal")
    );

    when(conexionMock.siguienteHecho(any(URL.class), any()))
        .thenReturn(fakeHecho)
        .thenReturn(null); // segunda llamada retorna null (fin del stream de hechos)

    fuenteDemo.actualizarHechos(); // 💡 llamás directamente

    List<Hecho> hechos = fuenteDemo.obtenerHechos();
    assertEquals(1, hechos.size());
    assertEquals("Incendio en zona rural",
                 hechos.get(0)
                       .getTitulo()
    );
  }

  @Test
  void noActualizaCacheCuandoNoHayHechos() {
    when(conexionMock.siguienteHecho(any(URL.class), any())).thenReturn(null);

    fuenteDemo.actualizarHechos();

    List<Hecho> hechos = fuenteDemo.obtenerHechos();
    assertTrue(hechos.isEmpty());
  }

  @Test
  void testLanzarExcepcionCustom() {
    when(conexionMock.siguienteHecho(any(URL.class), any()))
        .thenThrow(new RuntimeException("Error forzado"));

    ConexionFuenteDemoException exception = assertThrows(
        ConexionFuenteDemoException.class,
        () -> fuenteDemo.actualizarHechos()
    );

    assertTrue(exception.getMessage()
                        .contains("Error al consultar FuenteDemo"));
    assertInstanceOf(RuntimeException.class, exception.getCause());
  }

  @Test
  void seObtienenMultiplesHechosYSeGuardaEnCache() {
    Map<String, Object> hecho1 = Map.of(
        "titulo", "Incendio A",
        "descripcion", "Fuego en el bosque.",
        "categoria", "Incendios",
        "direccion", "Ruta A",
        "ubicacion", Map.of("latitud", -31.0, "longitud", -64.0),
        "fechaSuceso", "2024-05-01T15:30:00",
        "fechaCarga", "2024-06-01T12:00:00",
        "fuenteOrigen", "FUENTE_DEMO",
        "etiquetas", List.of("fuego")
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

    fuenteDemo.actualizarHechos();

    List<Hecho> hechos = fuenteDemo.obtenerHechos();
    assertEquals(2, hechos.size());
    assertEquals("Incendio A",
                 hechos.get(0)
                       .getTitulo()
    );
    assertEquals("Incendio B",
                 hechos.get(1)
                       .getTitulo()
    );
  }

  @Test
  void seProduceErrorSiLosDatosSonInvalidos() {
    Map<String, Object> hechoInvalido = Map.of(
        "titulo", "Hecho inválido",
        "descripcion", "Falla en datos",
        "categoria", "Otro",
        "direccion", "Calle falsa",
        "ubicacion", "ubicacion erronea",
        "fechaSuceso", "2024-05-01T15:30:00",
        "fechaCarga", "2024-06-01T12:00:00",
        "fuenteOrigen", "FUENTE_DEMO",
        "etiquetas", List.of("invalido")
    );

    when(conexionMock.siguienteHecho(any(), any())).thenReturn(hechoInvalido);

    assertThrows(ConexionFuenteDemoException.class, () -> fuenteDemo.actualizarHechos());
  }
}