package ar.edu.utn.frba.dds.domain.CSV;

import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.function.Function;

public class MapeoCSV {
  public Function<Map<String, String>, String> obtenerTitulo;
  public Function<Map<String, String>, String> obtenerDescripcion;
  public Function<Map<String, String>, String> obtenerCategoria;
  public Function<Map<String, String>, String> obtenerDireccion;
  public Function<Map<String, String>, LocalDateTime> obtenerFecha;
  public Function<Map<String, String>, PuntoGeografico> obtenerUbicacion;
}
