package ar.edu.utn.frba.dds;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNotNull;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ar.edu.utn.frba.dds.domain.exceptions.ConexionFuenteDemoException;
import ar.edu.utn.frba.dds.domain.fuentes.apis.AdapterDemo;
import ar.edu.utn.frba.dds.domain.fuentes.apis.conexion.Conexion;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AdapterDemoTest {

  // --- Tests para AdapterDemo  ---
  @Nested
  @DisplayName("Pruebas de lógica de AdapterDemo")
  class AdapterDemoLogicTest {

    @Mock
    private Conexion conexionMock;
    private AdapterDemo adapter;
    private URL dummyUrl;

    @BeforeEach
    void setUp() throws Exception {
      dummyUrl = new URL("http://dummy.url/hechos");
      adapter = new AdapterDemo(conexionMock, dummyUrl);
    }

    @Test
    @DisplayName("Transforma correctamente los Maps de la conexión en objetos Hecho")
    void transformaCorrectamenteLosDatos() throws IOException {
      Map<String, Object> hechoMap = Map.of(
          "titulo", "Incendio Forestal", "categoria", "Incendios",
          "fechaSuceso", "2024-05-01T10:00:00",
          "ubicacion", Map.of("latitud", -34.0, "longitud", -58.0)
      );
      when(conexionMock.siguienteHecho(any(URL.class), isNull())).thenReturn(hechoMap).thenReturn(null);

      List<Hecho> resultado = adapter.consultarHechos();

      assertNotNull(resultado);
      assertEquals(1, resultado.size());
      assertEquals("Incendio Forestal", resultado.get(0).getTitulo());
      assertEquals(-34.0, resultado.get(0).getUbicacion().getLatitud());
    }

    @Test
    @DisplayName("Omite hechos con datos inválidos y continúa")
    void omiteHechosInvalidosYContinua() throws IOException {
      Map<String, Object> hechoValido = Map.of("titulo", "Hecho Válido", "categoria", "Validos", "fechaSuceso", "2024-05-01T10:00:00");
      Map<String, Object> hechoInvalido = Map.of("categoria", "Invalidos"); // Sin título

      when(conexionMock.siguienteHecho(any(URL.class), isNull())).thenReturn(hechoInvalido).thenReturn(hechoValido).thenReturn(null);

      List<Hecho> resultado = adapter.consultarHechos();

      assertEquals(1, resultado.size());
      assertEquals("Hecho Válido", resultado.get(0).getTitulo());
    }

    @Test
    @DisplayName("Devuelve una lista vacía si la conexión no devuelve ningún hecho")
    void devuelveListaVaciaSiConexionNoTieneHechos() throws IOException {
      when(conexionMock.siguienteHecho(any(URL.class), any())).thenReturn(null);
      List<Hecho> resultado = adapter.consultarHechos();
      assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("Lanza ConexionFuenteDemoException si falla la conexión")
    void consultarHechosLanzaExcepcionSiFallaConexion() {
      when(conexionMock.siguienteHecho(any(URL.class), isNull())).thenThrow(new RuntimeException("Fallo de red"));
      assertThrows(ConexionFuenteDemoException.class, () -> adapter.consultarHechos());
    }

    @Test
    @DisplayName("Genera la configuración en formato JSON correctamente")
    void getConfiguracionJsonEsCorrecta() throws JsonProcessingException {
      String configJson = adapter.getConfiguracionJson();
      JsonNode node = new ObjectMapper().readTree(configJson);
      assertEquals("DEMO", node.get("tipo").asText());
      assertEquals(dummyUrl.toString(), node.get("url").asText());
    }

    @Test
    @DisplayName("Usa la fecha de última actualización en consultas subsecuentes")
    void usaFechaUltimaActualizacion() throws IOException {
      Map<String, Object> hecho1 = Map.of("titulo", "Primer Hecho", "categoria", "Cat1", "fechaSuceso", "2024-01-01T10:00:00");
      Map<String, Object> hecho2 = Map.of("titulo", "Segundo Hecho", "categoria", "Cat2", "fechaSuceso", "2024-01-01T11:00:00");

      when(conexionMock.siguienteHecho(any(URL.class), isNull())).thenReturn(hecho1).thenReturn(null);
      when(conexionMock.siguienteHecho(any(URL.class), isNotNull())).thenReturn(hecho2).thenReturn(null);

      // Primera llamada
      List<Hecho> primeraLlamada = adapter.consultarHechos();
      assertEquals(1, primeraLlamada.size());
      assertEquals("Primer Hecho", primeraLlamada.get(0).getTitulo());

      // Segunda llamada
      List<Hecho> segundaLlamada = adapter.consultarHechos();
      assertEquals(1, segundaLlamada.size());
      assertEquals("Segundo Hecho", segundaLlamada.get(0).getTitulo());
    }

    @Test
    @DisplayName("Construye un hecho correctamente con campos opcionales nulos")
    void construyeHechoConCamposNulos() throws IOException {
      Map<String, Object> hechoConNulos = Map.of(
          "titulo", "Título Obligatorio",
          "categoria", "Cat Obligatoria",
          "fechaSuceso", "2024-05-01T10:00:00"
          // Resto de los campos son nulos
      );
      when(conexionMock.siguienteHecho(any(URL.class), isNull())).thenReturn(hechoConNulos).thenReturn(null);

      List<Hecho> resultado = adapter.consultarHechos();
      assertEquals(1, resultado.size());
      Hecho hecho = resultado.get(0);
      assertEquals("Título Obligatorio", hecho.getTitulo());
      assertNull(hecho.getDescripcion());
      assertNull(hecho.getUbicacion());
    }
  }

  // --- Tests for Conexion ---
  @Nested
  @DisplayName("Pruebas para la clase Conexion")
  class ConexionTest {
    private Conexion conexion;
    private URL url;
    private HttpURLConnection connectionMock;

    @BeforeEach
    void setUp() throws IOException {
      connectionMock = mock(HttpURLConnection.class);
      conexion = new Conexion() {
        @Override
        protected HttpURLConnection createConnection(URL urlToConnect) {
          return connectionMock;
        }
      };
      url = new URL("http://test.url/api");
    }

    @Test
    @DisplayName("siguienteHecho retorna null si el código es 204 No Content")
    void siguienteHechoRetornaNullCon204() throws Exception {
      when(connectionMock.getResponseCode()).thenReturn(204);
      Map<String, Object> resultado = conexion.siguienteHecho(url, null);
      assertNull(resultado);
    }

    @Test
    @DisplayName("siguienteHecho lanza RuntimeException si el código es de error (ej. 500)")
    void siguienteHechoLanzaExcepcionConError500() throws Exception {
      when(connectionMock.getResponseCode()).thenReturn(500);
      RuntimeException exception = assertThrows(RuntimeException.class, () -> conexion.siguienteHecho(url, null));
      assertTrue(exception.getMessage().contains("Error en la consulta: 500"));
    }

    @Test
    @DisplayName("siguienteHecho procesa el JSON correctamente con código 200 OK")
    void siguienteHechoProcesaJsonCon200() throws Exception {
      String jsonResponse = "{\"titulo\":\"Test Titulo\",\"valor\":123}";
      InputStream inputStream = new ByteArrayInputStream(jsonResponse.getBytes(StandardCharsets.UTF_8));

      when(connectionMock.getResponseCode()).thenReturn(200);
      when(connectionMock.getInputStream()).thenReturn(inputStream);

      Map<String, Object> resultado = conexion.siguienteHecho(url, LocalDateTime.now());

      assertNotNull(resultado);
      assertEquals("Test Titulo", resultado.get("titulo"));
      assertEquals(123, ((Number) resultado.get("valor")).intValue());
    }

    @Test
    @DisplayName("siguienteHecho lanza RuntimeException si falla la conexión")
    void siguienteHechoLanzaExcepcionPorFallaDeConexion() throws Exception {
      when(connectionMock.getResponseCode()).thenThrow(new IOException("Error de red simulado"));
      RuntimeException exception = assertThrows(RuntimeException.class, () -> conexion.siguienteHecho(url, null));
      assertTrue(exception.getMessage().contains("Fallo la conexión"));
      assertTrue(exception.getCause() instanceof IOException);
    }
  }
}

