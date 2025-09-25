package ar.edu.utn.frba.dds.domain.lector.configuracion;

import ar.edu.utn.frba.dds.domain.hecho.CampoHecho;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.lector.Lector;
import ar.edu.utn.frba.dds.domain.lector.csv.LectorCSV;
import ar.edu.utn.frba.dds.domain.lector.csv.filaconverter.FilaConverter;
import ar.edu.utn.frba.dds.domain.lector.csv.filaconverter.HechoFilaConverter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.*;

@Entity
@DiscriminatorValue("CSV")
public class ConfiguracionLectorCsv extends ConfiguracionLector {

  private char separador;
  private String formatoFecha;

  // Con esto, JPA creará una tabla separada para guardar el mapeo de columnas.
  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "config_lector_csv_mapeo", joinColumns = @JoinColumn(name = "config_id"))
  @MapKeyEnumerated(EnumType.STRING) // La clave del mapa es un Enum
  @Column(name = "nombre_columna_csv")
  private Map<CampoHecho, List<String>> mapeoColumnas = new HashMap<>();

  // Constructor para JPA
  public ConfiguracionLectorCsv() {}

  // Constructor para uso general
  public ConfiguracionLectorCsv(char separador, String formatoFecha, Map<CampoHecho, List<String>> mapeoColumnas) {
    this.separador = separador;
    this.formatoFecha = formatoFecha;
    this.mapeoColumnas = mapeoColumnas;
  }

  @Override
  public <T> Lector<T> build(Class<T> clazz) {
    // Decide qué FilaConverter usar basado en la clase.
    if (clazz.equals(Hecho.class)) {
      FilaConverter<Hecho> converter = new HechoFilaConverter(formatoFecha, mapeoColumnas);

      // El cast es seguro gracias a la validación.
      @SuppressWarnings("unchecked")
      Lector<T> lector = (Lector<T>) new LectorCSV<>(separador, converter);
      return lector;
    }

    throw new IllegalArgumentException("No hay un FilaConverter definido para la clase: " + clazz.getName());
  }
}