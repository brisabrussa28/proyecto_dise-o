package ar.edu.utn.frba.dds.domain.serializadores.lector.csv.filaconverter;

import ar.edu.utn.frba.dds.domain.hecho.CampoHecho;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.hecho.HechoBuilder;
import ar.edu.utn.frba.dds.domain.hecho.Origen;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
 */
public class HechoFilaConverter implements FilaConverter<Hecho> {

  private static final Logger logger = Logger.getLogger(HechoFilaConverter.class.getName());

  private static final Set<CampoHecho> CAMPOS_REQUERIDOS = Set.of(
      CampoHecho.TITULO,
      CampoHecho.FECHA_SUCESO
  );

  private final String dateFormatStr;
  private final Map<CampoHecho, List<String>> mapeoColumnas;
  private final SimpleDateFormat dateFormat;

  public HechoFilaConverter(String formatoFecha, Map<CampoHecho, List<String>> mapeoColumnas) {
    if (formatoFecha == null || formatoFecha.isBlank() || mapeoColumnas == null || mapeoColumnas.isEmpty()) {
      throw new IllegalArgumentException(
          "El formato de fecha y el mapeo de columnas son obligatorios.");
    }
    this.dateFormatStr = formatoFecha;
    this.dateFormat = new SimpleDateFormat(formatoFecha);
    this.mapeoColumnas = new HashMap<>(mapeoColumnas);
  }

  @Override
  public Hecho convert(Map<String, String> fila) {
    if (!validadorDeFila(fila)) {
      return null;
    }

    String latitudStr = obtenerValor(fila, CampoHecho.LATITUD);
    String longitudStr = obtenerValor(fila, CampoHecho.LONGITUD);

    HechoBuilder builder = new HechoBuilder();

    builder.conTitulo(obtenerValor(fila, CampoHecho.TITULO));
    builder.conDescripcion(obtenerValor(fila, CampoHecho.DESCRIPCION));
    builder.conCategoria(obtenerValor(fila, CampoHecho.CATEGORIA));
    builder.conDireccion(obtenerValor(fila, CampoHecho.DIRECCION));
    builder.conProvincia(obtenerValor(fila, CampoHecho.PROVINCIA));
    builder.conFechaSuceso(parseFecha(obtenerValor(fila, CampoHecho.FECHA_SUCESO)));
    builder.conFuenteOrigen(Origen.DATASET); // Origen por defecto para CSV
    builder.conFechaCarga(LocalDateTime.now()); // Fecha de carga es ahora

    Double latitud = parseDouble(latitudStr);
    Double longitud = parseDouble(longitudStr);

    if (latitud != null && longitud != null) {
      builder.conUbicacion(new PuntoGeografico(latitud, longitud));
    }

    return builder.build();
  }

  private boolean validadorDeFila(Map<String, String> fila) {
    return CAMPOS_REQUERIDOS.stream()
                            .allMatch(campo -> {
                              String valor = obtenerValor(fila, campo);
                              boolean esValido = valor != null && !valor.isBlank();
                              if (!esValido) {
                                logger.warning("Fila ignorada: falta el campo requerido '" + campo + "' o está vacío.");
                              }
                              return esValido;
                            });
  }

  private String obtenerValor(Map<String, String> fila, CampoHecho campo) {
    List<String> posiblesColumnas = mapeoColumnas.get(campo);
    if (posiblesColumnas == null) {
      return null;
    }
    return posiblesColumnas.stream()
                           .map(fila::get)
                           .filter(Objects::nonNull)
                           .findFirst()
                           .orElse(null);
  }

  private String unirValores(List<String> valores, String separador) {
    return valores.stream()
                  .filter(s -> s != null && !s.isBlank())
                  .reduce((a, b) -> a + separador + b)
                  .orElse(null);
  }

  private Double parseDouble(String valor) {
    if (valor == null || valor.isBlank()) {
      return null;
    }
    try {
      // Reemplazar coma por punto para soportar varios formatos de número.
      String valorNormalizado = valor.trim()
                                     .replace(',', '.');
      return Double.parseDouble(valorNormalizado);
    } catch (NumberFormatException e) {
      logger.warning("Error al parsear double: '" + valor + "' no es un número válido.");
      return null;
    }
  }

  private LocalDateTime parseFecha(String valor) {
    if (valor == null || valor.isBlank()) {
      return null;
    }
    try {
      return dateFormat.parse(valor)
                       .toInstant()
                       .atZone(ZoneId.systemDefault())
                       .toLocalDateTime();
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
    // Convierte el mapa interno (con claves Enum) a un mapa con claves String
    // para que pueda ser serializado a JSON correctamente.
    return this.mapeoColumnas.entrySet()
                             .stream()
                             .collect(Collectors.toMap(
                                 entry -> entry.getKey()
                                               .name(), // Convierte el Enum a String
                                 Map.Entry::getValue
                             ));
  }
}

