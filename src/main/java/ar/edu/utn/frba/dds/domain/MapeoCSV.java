package ar.edu.utn.frba.dds.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class MapeoCSV {
  public Function<Map<String, String>, String> obtenerTitulo;
  public Function<Map<String, String>, String> obtenerDescripcion;
//  public Function<Map<String, String>, String> obtenerCategoria;
  public Function<Map<String, String>, String> obtenerDireccion;
  public Function<Map<String, String>, LocalDateTime> obtenerFecha;
  public Function<Map<String, String>, PuntoGeografico> obtenerUbicacion;
  public Function<Map<String, String>, List<Etiqueta>> obtenerEtiquetas;
}
