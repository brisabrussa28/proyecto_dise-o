package ar.edu.utn.frba.dds.domain.serializadores.exportador.csv;

import ar.edu.utn.frba.dds.domain.serializadores.exportador.Exportador;
import ar.edu.utn.frba.dds.domain.serializadores.exportador.csv.modoexportacion.ModoExportacion;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Exporta cualquier lista de objetos a el path que se le asigne en formato CSV
 *
 * @param <T> El tipo de objeto a exportar.
 */
public class ExportadorCSV<T> implements Exportador<T> {
  private static final Logger logger = Logger.getLogger(ExportadorCSV.class.getName());
  private final char separador;
  private final char quote;
  private final ModoExportacion modoExportacion;

  public ExportadorCSV(char separador, char quote, ModoExportacion modoExportacion) {
    this.separador = separador;
    this.quote = quote;
    this.modoExportacion = modoExportacion;
  }

  public ExportadorCSV(ModoExportacion modoExportacion) {
    this(',', '\"', modoExportacion);
  }

  @Override
  public void exportar(List<T> objetos, String path) {
    if (objetos == null || objetos.isEmpty()) {
      return;
    }

    try {
      String finalPath = this.modoExportacion.obtenerPathFinal(path);
      boolean anexar = this.modoExportacion.debeAnexar();
      this.crearDirectoriosSiNoExisten(finalPath);

      try (
          Writer writer = new BufferedWriter(
              new OutputStreamWriter(
                  new FileOutputStream(finalPath, anexar), StandardCharsets.UTF_8))
      ) {
        final boolean escribirCabecera = !anexar || new File(finalPath).length() == 0;

        HeaderColumnNameMappingStrategy<T> strategy =
            this.crearEstrategiaDeMapeo(
                (Class<? extends T>) objetos.get(0)
                                            .getClass(),
                escribirCabecera
            );

        StatefulBeanToCsv<T> beanToCsv = new StatefulBeanToCsvBuilder<T>(writer)
            .withSeparator(this.separador)
            .withQuotechar(this.quote)
            .withMappingStrategy(strategy)
            .build();

        beanToCsv.write(objetos);
      }
    } catch (IOException | CsvDataTypeMismatchException | CsvRequiredFieldEmptyException e) {
      throw new RuntimeException("Error al exportar el archivo CSV en: " + path, e);
    }
  }

  private HeaderColumnNameMappingStrategy<T> crearEstrategiaDeMapeo(
      Class<? extends T> tipoObjeto, final boolean escribirCabecera) {
    HeaderColumnNameMappingStrategy<T> strategy = new HeaderColumnNameMappingStrategy<T>() {
      @Override
      public String[] generateHeader(T bean) throws CsvRequiredFieldEmptyException {
        String[] header = super.generateHeader(bean);
        return escribirCabecera ? header : new String[0];
      }
    };
    strategy.setType(tipoObjeto);
    return strategy;
  }

  private void crearDirectoriosSiNoExisten(String path) throws IOException {
    Path filePath = Paths.get(path);
    Path parentDir = filePath.getParent();
    if (parentDir != null && !Files.exists(parentDir)) {
      Files.createDirectories(parentDir);
    }
  }

  /**
   * Devuelve la configuración del exportador en formato JSON.
   *
   * @return Un string con la configuración en JSON.
   */
  @Override
  public String getConfiguracionJson() {
    ObjectMapper objectMapper = new ObjectMapper();
    ObjectNode configNode = objectMapper.createObjectNode();

    configNode.put("formato", "CSV");
    configNode.put("separador", String.valueOf(this.separador));
    configNode.put("quote", String.valueOf(this.quote));

    String modoStr = this.modoExportacion.getClass()
                                         .getSimpleName();
    if (modoStr.startsWith("Modo")) {
      modoStr = modoStr.substring(4);
    }
    configNode.put("modo", modoStr.toUpperCase());

    try {
      return objectMapper.writerWithDefaultPrettyPrinter()
                         .writeValueAsString(configNode);
    } catch (JsonProcessingException e) {
      logger.log(Level.SEVERE, "Error al generar la configuración JSON para ExportadorCSV", e);
      return "{\"error\":\"No se pudo generar la configuración\"}";
    }
  }
}
