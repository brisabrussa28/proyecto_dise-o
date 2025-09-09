package ar.edu.utn.frba.dds.domain.csv;

import ar.edu.utn.frba.dds.domain.hecho.CampoHecho;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.hecho.HechoBuilder;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.origen.Origen;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Lector de archivos CSV a objetos Hecho.
 */
public class LectorCSV {

  private static final Logger logger = Logger.getLogger(LectorCSV.class.getName());

  private static final Set<CampoHecho> CAMPOS_REQUERIDOS = Set.of(
      CampoHecho.TITULO,
      CampoHecho.FECHA_SUCESO
  );

  private final char separator;
  private final String dateFormatStr;
  private final Map<CampoHecho, List<String>> mapeoColumnas;

  public LectorCSV(
      char separator,
      String dateFormatStr,
      Map<CampoHecho, List<String>> mapeoColumnas
  ) {
    this.separator = separator;
    this.dateFormatStr = dateFormatStr;
    this.mapeoColumnas = new HashMap<>(mapeoColumnas);

    //validacion basica para evitar errores al crear hechos
    validarConfiguracion();
  }

  private void validarConfiguracion() {
    for (CampoHecho campoRequerido : CAMPOS_REQUERIDOS) {
      if (!this.mapeoColumnas.containsKey(campoRequerido) || this.mapeoColumnas.get(campoRequerido).isEmpty()) {
        throw new IllegalArgumentException(
            "Configuración inválida. El mapeo de columnas debe contener una entrada para el campo requerido: " + campoRequerido
        );
      }
    }
  }

  /**
   * Este método lee un archivo CSV y lo convierte en una lista de hechos.
   *
   * @param path Ruta del archivo CSV a leer.
   * @return Lista de objetos Hecho importados desde el CSV.
   */
  public List<Hecho> importar(String path) {
    List<Hecho> hechos = new ArrayList<>();
    SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatStr);
    dateFormat.setLenient(false);

    try (
        CSVReader reader = new CSVReaderBuilder(
            new InputStreamReader(new java.io.FileInputStream(path), StandardCharsets.UTF_8)
        ).withCSVParser(new CSVParserBuilder().withSeparator(separator).build()).build()
    ) {
      String[] headers = reader.readNext();
      if (headers == null || headers.length == 0) {
        throw new IllegalArgumentException("El archivo CSV no contiene encabezados");
      }

      Set<String> columnasPresentes = new HashSet<>(Arrays.asList(headers));
      String[] row;
      int linea = 1;

      while ((row = reader.readNext()) != null) {
        linea++;
        Map<String, String> fila = mapearFila(row, headers);
        Hecho hecho = construirHechoDesdeFila(fila, columnasPresentes, mapeoColumnas, dateFormat);

        if (hecho != null) {
          hechos.add(hecho);
        } else {
          logger.warning("[Línea " + linea + "] Fila descartada por datos insuficientes");
        }
      }
    } catch (IOException | CsvException e) {
      throw new RuntimeException("Error al leer el archivo CSV", e);
    }

    return hechos;
  }

  /**
   * Mapea una fila del CSV a un mapa de columnas.
   *
   * @param row     Fila del CSV como un array de Strings.
   * @param headers Encabezados del CSV como un array de Strings.
   * @return Mapa donde las claves son los encabezados y los valores son los datos de la fila.
   */
  private Map<String, String> mapearFila(String[] row, String[] headers) {
    Map<String, String> fila = new HashMap<>();
    Set<String> seenHeaders = new HashSet<>();

    for (int i = 0; i < headers.length && i < row.length; i++) {
      if (seenHeaders.contains(headers[i])) {
        throw new IllegalArgumentException("Header duplicado detectado: " + headers[i]);
      }
      seenHeaders.add(headers[i]);
      fila.put(headers[i], row[i] != null ? row[i].trim() : null);
    }
    return fila;
  }

  /**
   * Construye un objeto Hecho a partir de una fila del CSV.
   *
   * @param fila              Mapa que representa una fila del CSV.
   * @param columnasPresentes Conjunto de columnas presentes en el CSV.
   * @param mapeo             Mapeo de campos del hecho a columnas del CSV.
   * @param dateFormat        Formato de fecha para los campos de fecha en el CSV.
   * @return Objeto Hecho construido o null si faltan datos esenciales.
   */
  private Hecho construirHechoDesdeFila(
      Map<String, String> fila,
      Set<String> columnasPresentes,
      Map<CampoHecho, List<String>> mapeo,
      SimpleDateFormat dateFormat
  ) {

    String titulo = extraerCampo(fila, mapeo.get(CampoHecho.TITULO), columnasPresentes, " ");
    LocalDateTime fechaSuceso = parseFecha(
        extraerCampo(fila, mapeo.get(CampoHecho.FECHA_SUCESO), columnasPresentes, "/"),
        dateFormat
    );
    String descripcion = extraerCampo(fila, mapeo.get(CampoHecho.DESCRIPCION), columnasPresentes, " ");
    String categoria = extraerCampo(fila, mapeo.get(CampoHecho.CATEGORIA), columnasPresentes, " ");
    String direccion = extraerCampo(fila, mapeo.get(CampoHecho.DIRECCION), columnasPresentes, ", ");
    String provincia = extraerCampo(fila, mapeo.get(CampoHecho.PROVINCIA), columnasPresentes, ", ");
    String latStr = extraerCampo(fila, mapeo.get(CampoHecho.LATITUD), columnasPresentes, "");
    String lonStr = extraerCampo(fila, mapeo.get(CampoHecho.LONGITUD), columnasPresentes, "");
    Double latitud = parseDouble(latStr);
    Double longitud = parseDouble(lonStr);
    PuntoGeografico ubicacion = (latitud != null && longitud != null)
        ? new PuntoGeografico(latitud, longitud)
        : null;
    try {
      //Agregando el builder
      return new HechoBuilder()
          .conTitulo(titulo)
          .conFechaSuceso(fechaSuceso)
          .conDescripcion(descripcion)
          .conCategoria(categoria)
          .conDireccion(direccion)
          .conProvincia(provincia)
          .conUbicacion(ubicacion)
          .conFuenteOrigen(Origen.DATASET)
          .build();
    } catch (RuntimeException e) {
      logger.warning("Error al construir Hecho: " + e.getMessage());
      return null;
    }
  }

  /**
   * Extrae un campo de la fila del CSV, concatenando valores si hay múltiples columnas.
   *
   * @param fila              Mapa que representa una fila del CSV.
   * @param columnas          Lista de nombres de columnas a extraer.
   * @param columnasPresentes Conjunto de columnas presentes en el CSV.
   * @param separador         Separador a utilizar para concatenar los valores.
   * @return Valor concatenado de las columnas o null si no se encuentra ningún valor.
   */
  private String extraerCampo(
      Map<String, String> fila,
      List<String> columnas,
      Set<String> columnasPresentes,
      String separador
  ) {
    if (columnas == null) {
      return null;
    }
    return columnas.stream()
        .peek(col -> {
          if (!columnasPresentes.contains(col)) {
            logger.warning("Advertencia: columna " + col + " no encontrada en CSV");
          }
        })
        .map(fila::get)
        .filter(s -> s != null && !s.trim().isEmpty())
        .reduce((a, b) -> a + separador + b)
        .orElse(null);
  }

  /**
   * Intenta parsear un String a Double, manejando excepciones.
   *
   * @param valor String a parsear.
   * @return Double parseado o null si ocurre un error.
   */
  private Double parseDouble(String valor) {
    if (valor == null) return null;
    try {
      return Double.parseDouble(valor.trim());
    } catch (NumberFormatException e) {
      logger.warning("Error al parsear double: '" + valor + "'");
      return null;
    }
  }

  /**
   * Intenta parsear un String a Date, manejando excepciones.
   *
   * @param valor      String a parsear.
   * @param dateFormat Formato de fecha a utilizar para el parseo.
   * @return Date parseada o null si ocurre un error.
   */
  private LocalDateTime parseFecha(String valor, SimpleDateFormat dateFormat) {
    if (valor == null) return null;
    try {
      return dateFormat.parse(valor.trim()).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    } catch (ParseException e) {
      logger.warning("Error al parsear fecha: '" + valor + "' con formato '" + dateFormat.toPattern() + "'");
      return null;
    }
  }
}