package ar.edu.utn.frba.dds;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ar.edu.utn.frba.dds.domain.fuentes.apis.AdapterDemo;
import ar.edu.utn.frba.dds.domain.fuentes.apis.AdapterFactory;
import ar.edu.utn.frba.dds.domain.fuentes.apis.AdapterMetaMapa;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class AdapterFactoryTest {
  // --- Tests para AdapterFactory ---

    private final AdapterFactory factory = new AdapterFactory();
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("Crea un AdapterDemo correctamente a partir de JSON")
    void creoUnAdapterDeDemo() throws JsonProcessingException {
      String json = "{ \"tipo\": \"DEMO\", \"url\": \"https://demo.com\" }";
      JsonNode node = mapper.readTree(json);
      Object adapter = factory.create(node);
      assertTrue(adapter instanceof AdapterDemo);
    }

    @Test
    @DisplayName("Crea un AdapterMetaMapa correctamente a partir de JSON")
    void creoUnAdapterDeMetamapa() throws JsonProcessingException {
      String json = "{ \"tipo\": \"METAMAPA\", \"url\": \"https://demo.com\" }";
      JsonNode node = mapper.readTree(json);
      Object adapter = factory.create(node);
      assertTrue(adapter instanceof AdapterMetaMapa);
    }

    @Test
    @DisplayName("Lanza una excepción si el tipo de adapter en JSON es desconocido")
    void siElTipoEsInvalidoLanzaExcepcion() throws JsonProcessingException {
      String json = "{ \"tipo\": \"TIPO_INVALIDO\", \"url\": \"https://demo.com\" }";
      JsonNode nodo = mapper.readTree(json);
      assertThrows(IllegalArgumentException.class, () -> factory.create(nodo));

  }

}
