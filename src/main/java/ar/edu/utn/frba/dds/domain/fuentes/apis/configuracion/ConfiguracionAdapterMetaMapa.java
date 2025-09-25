package ar.edu.utn.frba.dds.domain.fuentes.apis.configuracion;

import ar.edu.utn.frba.dds.domain.fuentes.apis.AdapterMetaMapa;
import ar.edu.utn.frba.dds.domain.fuentes.apis.FuenteAdapter;
import ar.edu.utn.frba.dds.domain.fuentes.apis.serviciometamapa.HechoQuerys;
import ar.edu.utn.frba.dds.domain.fuentes.apis.serviciometamapa.ServicioMetaMapa;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.DiscriminatorValue;

@Entity
@DiscriminatorValue("METAMAPA")
public class ConfiguracionAdapterMetaMapa extends ConfiguracionAdapter {
  private String urlServicio;

  @Embedded // Incrusta los campos de HechoQuerys directamente en esta tabla
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