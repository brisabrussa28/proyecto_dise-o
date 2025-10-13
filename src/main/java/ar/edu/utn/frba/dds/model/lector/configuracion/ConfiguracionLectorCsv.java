package ar.edu.utn.frba.dds.model.lector.configuracion;

import ar.edu.utn.frba.dds.model.hecho.Hecho;
import ar.edu.utn.frba.dds.model.lector.Lector;
import ar.edu.utn.frba.dds.model.lector.csv.LectorCSV;
import ar.edu.utn.frba.dds.model.lector.csv.MapeoCSV;
import ar.edu.utn.frba.dds.model.lector.csv.filaconverter.FilaConverter;
import ar.edu.utn.frba.dds.model.lector.csv.filaconverter.HechoFilaConverter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.persistence.Transient;

/**
 * Entidad de configuración para un LectorCSV.
 * Persiste su configuración de forma normalizada usando la entidad MapeoCSV.
 */
//@Entity
//@DiscriminatorValue("CSV")
public class ConfiguracionLectorCsv extends ConfiguracionLector {

  private char separador;
  private String formatoFecha;


  //  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
//  @JoinColumn(name = "config_lector_csv_id")
  @Transient
  private List<MapeoCSV> mapeos = new ArrayList<>();

  // Constructor para JPA.
  public ConfiguracionLectorCsv() {
  }

  /**
   * Constructor principal que recibe un mapa genérico y lo transforma en entidades MapeoCSV.
   *
   * @param separador     Carácter separador del CSV.
   * @param formatoFecha  Formato de fecha a utilizar.
   * @param mapeoColumnas Mapa con clave String que representa el campo y valor como lista de nombres de columnas.
   */
  public ConfiguracionLectorCsv(
      char separador,
      String formatoFecha,
      Map<String, List<String>> mapeoColumnas
  ) {
    this.separador = separador;
    this.formatoFecha = formatoFecha;
    if (mapeoColumnas != null) {
      this.mapeos = mapeoColumnas.entrySet()
                                 .stream()
                                 .map(entry -> new MapeoCSV(entry.getKey(), entry.getValue()))
                                 .collect(Collectors.toList());
    }
  }

  /**
   * Construye el lector lógico (LectorCSV).
   * Transforma la lista de MapeoCSV de nuevo a un Map<String, List<String>> para pasárselo al FilaConverter.
   */
  @Override
  public <T> Lector<T> build(Class<T> clazz) {
    if (clazz.equals(Hecho.class)) {
      // 1. Convertir la lista de entidades MapeoCSV de nuevo a un mapa simple.
      Map<String, List<String>> mapeoParaConverter = this.mapeos.stream()
                                                                .collect(Collectors.toMap(
                                                                    MapeoCSV::getCampo,
                                                                    MapeoCSV::getNombresColumnas
                                                                ));

      // 2. Crear el FilaConverter con el mapa simple.
      FilaConverter<Hecho> converter = new HechoFilaConverter(formatoFecha, mapeoParaConverter);

      // 3. Crear y devolver el LectorCSV.
      @SuppressWarnings("unchecked")
      Lector<T> lector = (Lector<T>) new LectorCSV<>(separador, converter);
      return lector;
    }

    throw new IllegalArgumentException("No hay un FilaConverter definido para la clase: " + clazz.getName());
  }
}

