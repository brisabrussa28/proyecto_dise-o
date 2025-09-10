package ar.edu.utn.frba.dds.domain.serializadores.csv.Lector.FilaConverter;

import ar.edu.utn.frba.dds.domain.hecho.CampoHecho;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.hecho.HechoBuilder;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import ar.edu.utn.frba.dds.domain.origen.Origen;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

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

  public HechoFilaConverter(String dateFormatStr, Map<CampoHecho, List<String>> mapeoColumnas) {
    this.dateFormatStr = dateFormatStr;
    this.mapeoColumnas = mapeoColumnas;
    this.dateFormat = new SimpleDateFormat(dateFormatStr);
    this.dateFormat.setLenient(false);
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

  @Override
  public Hecho convert(Map<String, String> fila) {
    String titulo = extraerCampo(fila, mapeoColumnas.get(CampoHecho.TITULO), " ");
    LocalDateTime fechaSuceso = parseFecha(
        extraerCampo(fila, mapeoColumnas.get(CampoHecho.FECHA_SUCESO), "/")
    );
    String descripcion = extraerCampo(fila, mapeoColumnas.get(CampoHecho.DESCRIPCION), " ");
    String categoria = extraerCampo(fila, mapeoColumnas.get(CampoHecho.CATEGORIA), " ");
    String direccion = extraerCampo(fila, mapeoColumnas.get(CampoHecho.DIRECCION), ", ");
    String provincia = extraerCampo(fila, mapeoColumnas.get(CampoHecho.PROVINCIA), ", ");
    String latStr = extraerCampo(fila, mapeoColumnas.get(CampoHecho.LATITUD), "");
    String lonStr = extraerCampo(fila, mapeoColumnas.get(CampoHecho.LONGITUD), "");
    Double latitud = parseDouble(latStr);
    Double longitud = parseDouble(lonStr);
    PuntoGeografico ubicacion = (latitud != null && longitud != null)
        ? new PuntoGeografico(latitud, longitud)
        : null;

    try {
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
      logger.warning("Error al construir Hecho: " + e.getMessage() + " | Fila: " + fila.toString());
      return null;
    }
  }

  private String extraerCampo(Map<String, String> fila, List<String> columnas, String separador) {
    if (columnas == null) {
      return null;
    }
    return columnas.stream()
        .map(fila::get)
        .filter(s -> s != null && !s.isBlank())
        .reduce((a, b) -> a + separador + b)
        .orElse(null);
  }

  private String extraerCampo(Map<String, String> fila, List<String> columnas) {
    return extraerCampo(fila, columnas, "");
  }

  private Double parseDouble(String valor) {
    if (valor == null || valor.isBlank()) return null;
    try {
      return Double.parseDouble(valor);
    } catch (NumberFormatException e) {
      logger.warning("Error al parsear double: '" + valor + "'");
      return null;
    }
  }

  private LocalDateTime parseFecha(String valor) {
    if (valor == null || valor.isBlank()) return null;
    try {
      return dateFormat.parse(valor).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    } catch (ParseException e) {
      logger.warning("Error al parsear fecha: '" + valor + "' con formato '" + this.dateFormatStr + "'");
      return null;
    }
  }
}
