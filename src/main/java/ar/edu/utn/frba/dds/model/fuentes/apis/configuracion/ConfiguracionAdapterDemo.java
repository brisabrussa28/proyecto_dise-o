package ar.edu.utn.frba.dds.model.fuentes.apis.configuracion;

import ar.edu.utn.frba.dds.model.fuentes.apis.AdapterDemo;
import ar.edu.utn.frba.dds.model.fuentes.apis.FuenteAdapter;
import ar.edu.utn.frba.dds.model.fuentes.apis.conexion.Conexion;
import java.net.MalformedURLException;
import java.net.URL;

//@Entity
//@DiscriminatorValue("DEMO")
public class ConfiguracionAdapterDemo extends ConfiguracionAdapter {
  private String url;

  public ConfiguracionAdapterDemo() {
  }

  public ConfiguracionAdapterDemo(String url) {
    this.url = url;
  }

  @Override
  public FuenteAdapter build() {
    try {
      return new AdapterDemo(new Conexion(), new URL(this.url));
    } catch (MalformedURLException e) {
      throw new RuntimeException("URL mal formada al construir AdapterDemo", e);
    }
  }
}