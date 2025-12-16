package ar.edu.utn.frba.dds.model.lector.csv.filaconverter.model.fuentes.apis.configuracion;

import ar.edu.utn.frba.dds.model.fuentes.apis.AdapterMetaMapa;
import ar.edu.utn.frba.dds.model.fuentes.apis.FuenteAdapter;
import ar.edu.utn.frba.dds.model.fuentes.apis.configuracion.ConfiguracionAdapter;
import ar.edu.utn.frba.dds.model.fuentes.apis.serviciometamapa.HechoQuerys;
import ar.edu.utn.frba.dds.model.fuentes.apis.serviciometamapa.ServicioMetaMapa;

//@Entity
//@DiscriminatorValue("METAMAPA")
public class ConfiguracionAdapterMetaMapa extends ConfiguracionAdapter {
  private String urlServicio;

  //@Embedded // Incrusta los campos de HechoQuerys directamente en esta tabla
  private HechoQuerys query;

  public ConfiguracionAdapterMetaMapa() {
  }

  public ConfiguracionAdapterMetaMapa(String url, HechoQuerys query) {
    this.urlServicio = url;
    this.query = query;
  }

  @Override
  public FuenteAdapter build() {
    ServicioMetaMapa servicio = new ServicioMetaMapa(this.urlServicio);
    return new AdapterMetaMapa(servicio, this.query);
  }
}