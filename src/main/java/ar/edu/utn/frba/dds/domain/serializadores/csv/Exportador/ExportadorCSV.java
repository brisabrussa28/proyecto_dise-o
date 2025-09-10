package ar.edu.utn.frba.dds.domain.serializadores.csv.Exportador;

import ar.edu.utn.frba.dds.domain.serializadores.csv.Exportador.ModoExportacion.ModoExportacion;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
/*
  NOTA DE NICO: El exportador actualmente es un exportador homogeneo, esto pq la manera mas facil de
  trabajar con BeanToCSV y OpenCSV (y la mas normal) es cuando los objetos tienen los mismos atributos
  (Son Homogeneos) Si queremos en unfuturo plantearlo como un no homogeneo lo vemos, pero es un lio

  Lo otro que se puede plantear si en un futuro deseamos exportar clases con muchos atributos distintos
  es crear una clase que tenga todos los atributos y nos sirva como mapeo original

  Plantear una solucion no homogenea tambien es posible pero es un lio, deberiamos recorrer toda
  la lista de objetos analizar sus atributos, extraerlos en headers. seria un lio.
 */


/**
 * Exporta cualquier lista de objetos a el path que se le asigne en formato CSV
 * @param <T> El tipo de objeto a exportar.
 */
public class ExportadorCSV<T> {
  private final char separador;
  private final char quote;
  private final ModoExportacion modoExportacion;

  /**
   * Constructor completo
   * @param separador Caracter que se va a utilizar para separar las columnas
   * @param quote Caracter que se va a utilizar para poner el texto (permite, por ej, poner comas dentro de columnas)
   * @param modoExportacion Modo de exportacion para el archivo csv
   */
  public ExportadorCSV(char separador, char quote, ModoExportacion modoExportacion) {
    this.separador = separador;
    this.quote = quote;
    this.modoExportacion = modoExportacion;
  }
  /**
   * Constructor estandar. (con el separador coma y el quote \" que son los estandar para csv
   * @param modoExportacion Modo de exportacion para el archivo csv
   */
  public ExportadorCSV(ModoExportacion modoExportacion) {
    this(',', '\"', modoExportacion);
  }

  /**
   * Exporta la lista de objetos en un archivo csv ubicadop en el path segun el modo elegido
   * @param objetos Lista de objetos a exportar en el csv
   * @param path Path del archivo
   */
  public void exportar(List<T> objetos, String path) {
    if (objetos == null || objetos.isEmpty()) {
      return;
    }

    try {
      String finalPath = this.modoExportacion.obtenerPathFinal(path);
      boolean anexar = this.modoExportacion.debeAnexar();
      this.crearDirectoriosSiNoExisten(finalPath);

      try (Writer writer = new FileWriter(finalPath, anexar)) {
        final boolean escribirCabecera = !anexar || new File(finalPath).length() == 0;

        HeaderColumnNameMappingStrategy<T> strategy =
            this.crearEstrategiaDeMapeo(
                (Class<? extends T>) objetos.get(0).getClass(),
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

  /**
   * Controla si la cabecera del CSV se escribe o no.
   * @param tipoObjeto La clase del objeto que se está exportando.
   * @param escribirCabecera Un booleano que indica si se debe escribir la cabecera.
   * @return Una estrategia de mapeo para OpenCSV.
   */
  private HeaderColumnNameMappingStrategy<T> crearEstrategiaDeMapeo(
      Class<? extends T> tipoObjeto, final boolean escribirCabecera) {
    HeaderColumnNameMappingStrategy<T> strategy = new HeaderColumnNameMappingStrategy<T>() {
      @Override
      public String[] generateHeader(T bean) throws CsvRequiredFieldEmptyException {
        String[] header = super.generateHeader(bean);

        // Devuelve la cabecera o nada según corresponda.
        return escribirCabecera ? header : new String[0];
      }
    };
    strategy.setType(tipoObjeto);
    return strategy;
  }

  /**
   * Crea los directorios padre del  archivo si es que no existen
   * @param path el path donde se va a crear el archivo
   */
  private void crearDirectoriosSiNoExisten(String path) throws IOException {
    Path filePath = Paths.get(path);
    Path parentDir = filePath.getParent();
    if (parentDir != null && !Files.exists(parentDir)) {
      Files.createDirectories(parentDir);
    }
  }
}
