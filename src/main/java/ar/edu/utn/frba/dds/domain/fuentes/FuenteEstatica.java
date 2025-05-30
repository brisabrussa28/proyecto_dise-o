package ar.edu.utn.frba.dds.domain.fuentes;

import ar.edu.utn.frba.dds.domain.csv.UltraBindLector;
import ar.edu.utn.frba.dds.domain.hecho.CampoHecho;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Fuente de datos estática basada en archivo CSV.
 */
public class FuenteEstatica extends Fuente {

  private final String rutaCsv;
  private final char separador;
  private final String formatoFecha;
  private final Map<CampoHecho, List<String>> mapeo;

  public FuenteEstatica(String nombre, String rutaCsv, char separador, String formatoFecha, Map<CampoHecho, List<String>> mapeo) {
    super(nombre, null); // no carga los hechos en el constructor
    this.rutaCsv = rutaCsv;
    this.separador = separador;
    this.formatoFecha = formatoFecha;
    this.mapeo = mapeo;
  }

  @Override
  public List<Hecho> obtenerHechos() {
    List<Hecho> hechosCrudos = new UltraBindLector().importar(rutaCsv, separador, formatoFecha, mapeo);
    return hechosCrudos.stream().toList();
  }
}
