package ar.edu.utn.frba.dds.model.lector.csv.filaconverter;

import ar.edu.utn.frba.dds.model.hecho.CampoHecho;
import ar.edu.utn.frba.dds.model.hecho.Hecho;
import ar.edu.utn.frba.dds.model.hecho.HechoBuilder;
import ar.edu.utn.frba.dds.model.hecho.Origen;
import ar.edu.utn.frba.dds.model.hecho.multimedia.Multimedia;
import ar.edu.utn.frba.dds.model.info.PuntoGeografico;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
      CampoHecho.FECHA_SUCESO.name(),
      CampoHecho.LATITUD.name(),
      CampoHecho.LONGITUD.name(),
      CampoHecho.DIRECCION.name()
  );

  private final String dateFormatStr;
  private final Map<String, List<String>> mapeoColumnas;

  /**
   * Constructor que recibe la configuración necesaria para la conversión.
   *
   * @param formatoFecha  Formato de fecha esperado en el CSV (ej. "dd/MM/yyyy HH:mm:ss").
   * @param mapeoColumnas Mapa que asocia un campo de 'Hecho' con una o más columnas del CSV.
   */
  public HechoFilaConverter(String formatoFecha, Map<String, List<String>> mapeoColumnas) {
    if (formatoFecha == null || formatoFecha.isBlank() || mapeoColumnas == null || mapeoColumnas.isEmpty()) {
      throw new IllegalArgumentException(
          "El formato de fecha y el mapeo de columnas son obligatorios.");
    }
    this.dateFormatStr = formatoFecha;
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
      logger.warning("Fila ignorada por no cumplir con los campos requeridos");
      return null;
    }

    HechoBuilder builder = new HechoBuilder();

    // Configuración del builder a partir de la fila - Campos simples
    obtenerPrimerValor(fila, CampoHecho.TITULO.name()).ifPresent(builder::conTitulo);
    obtenerPrimerValor(fila, CampoHecho.CATEGORIA.name()).ifPresent(builder::conCategoria);
    obtenerPrimerValor(fila, CampoHecho.PROVINCIA.name()).ifPresent(builder::conProvincia);

    // Fechas
    obtenerPrimerValor(fila, CampoHecho.FECHA_SUCESO.name())
        .map(this::parseFecha)
        .ifPresent(builder::conFechaSuceso);

    obtenerPrimerValor(fila, CampoHecho.FECHA_CARGA.name())
        .map(this::parseFecha)
        .ifPresentOrElse(builder::conFechaCarga, () -> builder.conFechaCarga(LocalDateTime.now()));

    // Para campos que pueden estar compuestos por varias columnas (ej. calle + altura), se unen todos los valores.
    String direccionCompleta = unirValores(
        obtenerTodosLosValores(
            fila,
            CampoHecho.DIRECCION.name()
        ), ", "
    );
    builder.conDireccion(direccionCompleta);

    String descripcionCompleta = unirValores(
        obtenerTodosLosValores(
            fila,
            CampoHecho.DESCRIPCION.name()
        ), " "
    );
    builder.conDescripcion(descripcionCompleta);

    // Lógica para coordenadas geográficas
    String latitudStr = obtenerPrimerValor(fila, CampoHecho.LATITUD.name()).orElse(null);
    String longitudStr = obtenerPrimerValor(fila, CampoHecho.LONGITUD.name()).orElse(null);
    Double latitud = parseDouble(latitudStr);
    Double longitud = parseDouble(longitudStr);
    if (latitud != null && longitud != null) {
      builder.conUbicacion(new PuntoGeografico(latitud, longitud));
    }

    // --- CAMBIO: Origen SIEMPRE es DATASET para fuentes estáticas ---
    builder.conFuenteOrigen(Origen.DATASET);

    // --- PROCESAMIENTO DE FOTOS ---
    List<String> urlsFotos = obtenerTodosLosValores(fila, CampoHecho.FOTOS.name());

    try {
      // Construimos el Hecho base
      Hecho hechoConstruido = builder.build();

      // Asignación tardía de fotos
      if (!urlsFotos.isEmpty()) {
        List<Multimedia> listaMultimedia = new ArrayList<>();
        for (String valorCelda : urlsFotos) {
          String[] urlsIndividuales = valorCelda.split("[,|;]");
          for (String urlStr : urlsIndividuales) {
            Multimedia m = descargarYCrearMultimedia(urlStr.trim());
            if (m != null) {
              listaMultimedia.add(m);
            }
          }
        }
        hechoConstruido.setFotos(listaMultimedia);
      }

      return hechoConstruido;

    } catch (RuntimeException e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  /**
   * Intenta descargar una imagen desde una URL y crear un objeto Multimedia.
   * Si falla, retorna null pero no detiene la importación.
   */
  private Multimedia descargarYCrearMultimedia(String urlStr) {
    try {
      if (!urlStr.startsWith("http")) {
        return null; // Ignoramos rutas locales o inválidas por seguridad
      }
      URL url = new URL(urlStr);

      String fileName = urlStr.substring(urlStr.lastIndexOf('/') + 1);
      if (fileName.isEmpty()) {
        fileName = "imagen_importada.jpg";
      }

      try (
          InputStream in = url.openStream();
          ByteArrayOutputStream out = new ByteArrayOutputStream()
      ) {

        byte[] buffer = new byte[1024];
        int n;
        while (-1 != (n = in.read(buffer))) {
          out.write(buffer, 0, n);
        }
        byte[] bytes = out.toByteArray();

        String contentType = "image/jpeg";
        if (fileName.toLowerCase()
                    .endsWith(".png")) {
          contentType = "image/png";
        }

        return new Multimedia(fileName, contentType, bytes);
      }
    } catch (Exception e) {
      logger.warning("No se pudo descargar la imagen de la URL: " + urlStr + ". Error: " + e.getMessage());
      return null;
    }
  }

  /**
   * Valida si una fila contiene todos los campos requeridos con valores no vacíos.
   *
   * @param fila La fila a validar.
   * @return true si la fila es válida, false en caso contrario.
   */
  private boolean validadorDeFila(Map<String, String> fila) {
    List<String> camposFaltantes = CAMPOS_REQUERIDOS.stream()
                                                    .filter(campo -> !obtenerPrimerValor(
                                                        fila,
                                                        campo
                                                    ).isPresent())
                                                    .collect(Collectors.toList());

    if (!camposFaltantes.isEmpty()) {
      logger.warning("Campos requeridos faltantes o vacíos: " + camposFaltantes
                         + " | Columnas disponibles en la fila: " + fila.keySet());
      return false;
    }

    return true;
  }

  private List<String> obtenerTodosLosValores(Map<String, String> fila, String campo) {
    List<String> posiblesColumnas = mapeoColumnas.get(campo);
    if (posiblesColumnas == null) {
      return Collections.emptyList();
    }
    return posiblesColumnas.stream()
                           .map(fila::get)
                           .filter(s -> s != null && !s.isBlank())
                           .collect(Collectors.toList());
  }

  private String unirValores(List<String> valores, String separador) {
    if (valores == null || valores.isEmpty()) {
      return null;
    }
    return String.join(separador, valores);
  }

  private java.util.Optional<String> obtenerPrimerValor(Map<String, String> fila, String campo) {
    List<String> posiblesColumnas = mapeoColumnas.get(campo);
    if (posiblesColumnas == null) {
      return java.util.Optional.empty();
    }
    return posiblesColumnas.stream()
                           .map(fila::get)
                           .filter(s -> s != null && !s.isBlank())
                           .findFirst();
  }

  private Double parseDouble(String valor) {
    if (valor == null || valor.isBlank()) {
      return null;
    }
    try {
      return Double.parseDouble(valor.trim()
                                     .replace(',', '.'));
    } catch (NumberFormatException e) {
      logger.warning("Error al parsear double: '" + valor + "'");
      return null;
    }
  }

  /**
   * Convierte un String a LocalDateTime.
   * AHORA ES MÁS ROBUSTO: Si falla el parseo de Fecha+Hora, intenta solo Fecha y agrega 00:00.
   */
  private LocalDateTime parseFecha(String valor) {
    if (valor == null || valor.isBlank()) {
      return null;
    }
    valor = valor.trim();

    // Lista de formatos a probar en orden
    List<String> formatosPosibles = List.of(
        this.dateFormatStr,           // El formato configurado por el usuario tiene prioridad
        "dd/MM/yyyy HH:mm",
        "dd/MM/yyyy HH:mm:ss",
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd HH:mm",
        "dd/MM/yyyy",
        "yyyy-MM-dd",
        "yyyy-mm-ddThh:mm:ss.sss",
        "yyyy-mm-ddYhh:mm:ss.ssssss"
    );

    for (String formato : formatosPosibles) {
      DateTimeFormatter formatter;
      try {
        formatter = DateTimeFormatter.ofPattern(formato);
      } catch (IllegalArgumentException e) {
        continue; // Ignorar formatos inválidos
      }

      // 1. Intentar parsear como LocalDateTime (Fecha y Hora)
      try {
        return LocalDateTime.parse(valor, formatter);
      } catch (DateTimeException e) {
        // 2. Si falla, intentar parsear como LocalDate (Solo fecha) y agregar inicio del día
        try {
          return LocalDate.parse(valor, formatter)
                          .atStartOfDay();
        } catch (DateTimeException e2) {
          // Sigue sin coincidir, probar siguiente formato
        }
      }
    }

    logger.warning("Error al parsear fecha: '" + valor + "'. No coincide con ningún formato conocido.");
    return null;
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