package ar.edu.utn.frba.dds.model.lector.csv.filaconverter;

import ar.edu.utn.frba.dds.model.hecho.CampoHecho;
import ar.edu.utn.frba.dds.model.hecho.Hecho;
import ar.edu.utn.frba.dds.model.hecho.HechoBuilder;
import ar.edu.utn.frba.dds.model.hecho.Origen;
import ar.edu.utn.frba.dds.model.info.PuntoGeografico;

import ar.edu.utn.frba.dds.domain.hecho.CampoHecho;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.hecho.HechoBuilder;
import ar.edu.utn.frba.dds.domain.hecho.Origen;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Implementación de 'FilaConverter' específica para crear objetos 'Hecho'.
 * Contiene toda la lógica de negocio para transformar una fila de CSV en un 'Hecho'.
 * Utiliza Strings para las claves y ahora une valores de múltiples columnas para campos compuestos.
 */
public class HechoFilaConverter implements FilaConverter<Hecho> {

  private static final Logger logger = Logger.getLogger(HechoFilaConverter.class.getName());

  private static final Set<String> CAMPOS_REQUERIDOS = Set.of(
      CampoHecho.TITULO.name(),
      CampoHecho.FECHA_SUCESO.name()
  );

  private final String dateFormatStr;
  private final Map<String, List<String>> mapeoColumnas;
  private final SimpleDateFormat dateFormat;

  /**
   * Constructor que recibe la configuración necesaria para la conversión.
   *
   * @param formatoFecha  Formato de fecha esperado en el CSV (ej. "dd/MM/yyyy HH:mm:ss").
   * @param mapeoColumnas Mapa que asocia un campo de 'Hecho' con una o más columnas del CSV.
   */
  public HechoFilaConverter(String formatoFecha, Map<String, List<String>> mapeoColumnas) {
    if (formatoFecha == null || formatoFecha.isBlank() || mapeoColumnas == null || mapeoColumnas.isEmpty()) {
      throw new IllegalArgumentException("El formato de fecha y el mapeo de columnas son obligatorios.");
    }
    this.dateFormatStr = formatoFecha;
    this.dateFormat = new SimpleDateFormat(formatoFecha);
    this.mapeoColumnas = new HashMap<>(mapeoColumnas);
  }

  /**
   * Convierte una fila de CSV en un objeto Hecho.
   *
   * @param fila Un mapa donde la clave es el nombre de la columna y el valor es el dato de la celda.
   * @return Un Hecho, o null si la fila no es válida.
   */
  @Override
  public Hecho convert(Map<String, String> fila) {
    if (!validadorDeFila(fila)) {
      logger.warning("Fila ignorada por no cumplir con los campos requeridos.");
      return null;
    }

    HechoBuilder builder = new HechoBuilder();

    // Configuración del builder a partir de la fila
    obtenerPrimerValor(fila, CampoHecho.TITULO.name()).ifPresent(builder::conTitulo);
    obtenerPrimerValor(fila, CampoHecho.CATEGORIA.name()).ifPresent(builder::conCategoria);
    obtenerPrimerValor(fila, CampoHecho.PROVINCIA.name()).ifPresent(builder::conProvincia);
    obtenerPrimerValor(fila, CampoHecho.FECHA_SUCESO.name()).map(this::parseFecha).ifPresent(builder::conFechaSuceso);

    // Para campos que pueden estar compuestos por varias columnas (ej. calle + altura), se unen todos los valores.
    String direccionCompleta = unirValores(obtenerTodosLosValores(fila, CampoHecho.DIRECCION.name()), ", ");
    builder.conDireccion(direccionCompleta);

    String descripcionCompleta = unirValores(obtenerTodosLosValores(fila, CampoHecho.DESCRIPCION.name()), " ");
    builder.conDescripcion(descripcionCompleta);

    // Lógica para coordenadas geográficas
    String latitudStr = obtenerPrimerValor(fila, CampoHecho.LATITUD.name()).orElse(null);
    String longitudStr = obtenerPrimerValor(fila, CampoHecho.LONGITUD.name()).orElse(null);
    Double latitud = parseDouble(latitudStr);
    Double longitud = parseDouble(longitudStr);
    if (latitud != null && longitud != null) {
      builder.conUbicacion(new PuntoGeografico(latitud, longitud));
    }

    // Asigna valores por defecto o de sistema
    builder.conFuenteOrigen(Origen.DATASET);
    builder.conFechaCarga(LocalDateTime.now());

    // Se llama a build(), que ahora devuelve el Hecho directamente.
    return builder.build();
  }

  /**
   * Valida si una fila contiene todos los campos requeridos con valores no vacíos.
   *
   * @param fila La fila a validar.
   * @return true si la fila es válida, false en caso contrario.
   */
  private boolean validadorDeFila(Map<String, String> fila) {
    return CAMPOS_REQUERIDOS.stream().allMatch(campo -> obtenerPrimerValor(fila, campo).isPresent());
  }

  /**
   * Obtiene todos los valores de las columnas mapeadas para un campo específico.
   *
   * @param fila  El mapa que representa la fila del CSV.
   * @param campo El nombre del campo (como String) a buscar.
   * @return Una lista con todos los valores encontrados para ese campo.
   */
  private List<String> obtenerTodosLosValores(Map<String, String> fila, String campo) {
    List<String> posiblesColumnas = mapeoColumnas.get(campo);
    if (posiblesColumnas == null) return Collections.emptyList();
    return posiblesColumnas.stream().map(fila::get).filter(s -> s != null && !s.isBlank()).collect(Collectors.toList());
  }

  /**
   * Une una lista de strings con un separador, devolviendo null si la lista está vacía.
   *
   * @param valores   La lista de strings a unir.
   * @param separador El separador a usar entre elementos.
   * @return El string resultante o null.
   */
  private String unirValores(List<String> valores, String separador) {
    if (valores == null || valores.isEmpty()) return null;
    return String.join(separador, valores);
  }

  /**
   * Obtiene el primer valor no nulo de las columnas mapeadas para un campo.
   *
   * @param fila  El mapa que representa la fila del CSV.
   * @param campo El nombre del campo (como String) a buscar.
   * @return Un Optional con el valor si se encuentra, o un Optional vacío.
   */
  private java.util.Optional<String> obtenerPrimerValor(Map<String, String> fila, String campo) {
    List<String> posiblesColumnas = mapeoColumnas.get(campo);
    if (posiblesColumnas == null) return java.util.Optional.empty();
    return posiblesColumnas.stream().map(fila::get).filter(s -> s != null && !s.isBlank()).findFirst();
  }

  /**
   * Convierte un String a Double, manejando comas y errores de formato.
   */
  private Double parseDouble(String valor) {
    if (valor == null || valor.isBlank()) return null;
    try {
      return Double.parseDouble(valor.trim().replace(',', '.'));
    } catch (NumberFormatException e) {
      logger.warning("Error al parsear double: '" + valor + "'");
      return null;
    }
  }

  /**
   * Convierte un String a LocalDateTime usando el formato definido en el constructor.
   */
  private LocalDateTime parseFecha(String valor) {
    if (valor == null || valor.isBlank()) return null;
    try {
      return dateFormat.parse(valor).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    } catch (ParseException e) {
      logger.warning("Error al parsear fecha: '" + valor + "' con formato '" + this.dateFormatStr + "'");
      return null;
    }
  }

  @Override
  public String getFormatoFecha() {
    return this.dateFormatStr;
  }

  @Override
  public Map<String, List<String>> getMapeoColumnasParaJson() {
    return this.mapeoColumnas;
  }
}

