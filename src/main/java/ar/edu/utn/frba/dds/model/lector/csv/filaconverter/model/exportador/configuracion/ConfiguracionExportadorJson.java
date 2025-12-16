package ar.edu.utn.frba.dds.model.lector.csv.filaconverter.model.exportador.configuracion;


import ar.edu.utn.frba.dds.model.lector.csv.filaconverter.model.exportador.Exportador;
import ar.edu.utn.frba.dds.model.lector.csv.filaconverter.model.exportador.json.ExportadorJson;

//@Entity
//@DiscriminatorValue("JSON")
public class ConfiguracionExportadorJson extends ConfiguracionExportador {

  //  @Override
  public <T> Exportador<T> build() {
    return new ExportadorJson<>();
  }
}