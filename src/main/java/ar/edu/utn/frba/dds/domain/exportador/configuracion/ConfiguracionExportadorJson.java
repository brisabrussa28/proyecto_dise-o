package ar.edu.utn.frba.dds.domain.exportador.configuracion;


import ar.edu.utn.frba.dds.domain.exportador.Exportador;
import ar.edu.utn.frba.dds.domain.exportador.json.ExportadorJson;

//@Entity
//@DiscriminatorValue("JSON")
public class ConfiguracionExportadorJson extends ConfiguracionExportador {

  //  @Override
  public <T> Exportador<T> build() {
    return new ExportadorJson<>();
  }
}