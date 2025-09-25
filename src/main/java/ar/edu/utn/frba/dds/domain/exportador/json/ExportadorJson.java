package ar.edu.utn.frba.dds.domain.exportador.json;

import ar.edu.utn.frba.dds.domain.exportador.Exportador;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExportadorJson<T> implements Exportador<T> {

  private static final Logger LOGGER = Logger.getLogger(ExportadorJson.class.getName());
  private final ObjectMapper objectMapper;

  public ExportadorJson() {
    this.objectMapper = new ObjectMapper();
    this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    this.objectMapper.registerModule(new JavaTimeModule());
    this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
  }

  @Override
  public void exportar(List<T> objetos, String jsonFilePath) {
    try {
      objectMapper.writeValue(new File(jsonFilePath), objetos);
      LOGGER.info("Copia local de objetos guardada en " + jsonFilePath);
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Error al escribir la copia JSON local en " + jsonFilePath, e);
    }
  }

}