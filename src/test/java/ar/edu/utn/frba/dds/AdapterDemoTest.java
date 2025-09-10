package ar.edu.utn.frba.dds;

import ar.edu.utn.frba.dds.domain.fuentes.apis.AdapterDemo;
import ar.edu.utn.frba.dds.domain.fuentes.apis.Conexion.Conexion;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AdapterDemoTest {

  @Mock
  private Conexion conexionMock;

  private AdapterDemo adapter;

  @BeforeEach
  void setUp() throws Exception {
    URL dummyUrl = new URL("http://dummy.url/hechos");
    adapter = new AdapterDemo(conexionMock, dummyUrl);
  }

  @Test
  @DisplayName("Transfdorma correctamente los Maps de la conexión en objetos Hecho")
  void transformaCorrectamenteLosDatos() throws IOException {
    Map<String, Object> hechoMap1 = Map.of(
        "titulo", "Incendio Forestal",
        "categoria", "Incendios",
        "fechaSuceso", "2024-05-01T10:00:00",
        "ubicacion", Map.of("latitud", -34.0, "longitud", -58.0)
    );
    Map<String, Object> hechoMap2 = Map.of(
        "titulo", "Corte de Luz",
        "categoria", "Servicios",
        "fechaSuceso", "2024-05-02T15:30:00",
        "descripcion", "Corte programado"
    );

    when(conexionMock.siguienteHecho(any(URL.class), isNull()))
        .thenReturn(hechoMap1)
        .thenReturn(hechoMap2)
        .thenReturn(null);

    List<Hecho> resultado = adapter.consultarHechos();

    assertNotNull(resultado);
    assertEquals(2, resultado.size());

    Hecho primerHecho = resultado.get(0);
    assertEquals("Incendio Forestal", primerHecho.getTitulo());
    assertEquals("Incendios", primerHecho.getCategoria());
    assertEquals(-34.0, primerHecho.getUbicacion().getLatitud());

    Hecho segundoHecho = resultado.get(1);
    assertEquals("Corte de Luz", segundoHecho.getTitulo());
    assertEquals("Corte programado", segundoHecho.getDescripcion());
  }

  @Test
  @DisplayName("Omite hechos con datos inválidos")
  void omiteHechosInvalidosYContinua() throws IOException {
    Map<String, Object> hechoValido = Map.of(
        "titulo", "Hecho Válido",
        "categoria", "Validos",
        "fechaSuceso", "2024-05-01T10:00:00"
    );
    Map<String, Object> hechoInvalido = Map.of(
        "categoria", "Invalidos",
        "fechaSuceso", "2024-05-01T11:00:00"
    );

    when(conexionMock.siguienteHecho(any(URL.class), isNull()))
        .thenReturn(hechoInvalido)
        .thenReturn(hechoValido)
        .thenReturn(null);

    // Act
    List<Hecho> resultado = adapter.consultarHechos();

    // Assert
    assertEquals(1, resultado.size(), "Solo el hecho válido debe ser procesado");
    assertEquals("Hecho Válido", resultado.get(0).getTitulo());
  }

  @Test
  @DisplayName("Devuelve una lista vacía si la conexión no devuelve ningún hecho")
  void devuelveListaVaciaSiConexionNoTieneHechos() throws IOException {
    // Arrange
    when(conexionMock.siguienteHecho(any(URL.class), any())).thenReturn(null);

    // Act
    List<Hecho> resultado = adapter.consultarHechos();

    // Assert
    assertTrue(resultado.isEmpty());
  }
}