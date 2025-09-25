package ar.edu.utn.frba.dds.domain.fuentes;

import ar.edu.utn.frba.dds.domain.exportador.configuracion.ConfiguracionExportador;
import ar.edu.utn.frba.dds.domain.fuentes.apis.FuenteAdapter;
import ar.edu.utn.frba.dds.domain.fuentes.apis.configuracion.ConfiguracionAdapter;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.lector.configuracion.ConfiguracionLector;

import java.util.Collections;
import java.util.List;
import javax.persistence.*;

@Entity
@DiscriminatorValue("API_EXTERNA")
public class FuenteExternaAPI extends FuenteDeCopiaLocal {

  @Transient
  private FuenteAdapter adaptador;

  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
  private ConfiguracionAdapter configuracionAdapter;

  protected FuenteExternaAPI() {
    super();
  }

  public FuenteExternaAPI(
      String nombre,
      ConfiguracionAdapter configAdapter,
      String rutaCopia,
      ConfiguracionLector configLector,
      ConfiguracionExportador configExportador
  ) {
    super(nombre, rutaCopia, configLector, configExportador);
    this.configuracionAdapter = configAdapter;
    reconstruirDependencias();
  }

  //postload en fuentedecopialocal
  @Override
  protected void reconstruirDependencias() {
    super.reconstruirDependencias(); // Reconstruye Lector y Exportador del padre
    if (this.configuracionAdapter != null) {
      this.adaptador = this.configuracionAdapter.build(); // Reconstruye el Adapter
    }
  }

  @Override
  protected List<Hecho> consultarNuevosHechos() {
    if (this.adaptador == null) {
      // Loggear o manejar el error de que el adaptador no está configurado
      return Collections.emptyList();
    }
    try {
      return this.adaptador.consultarHechos();
    } catch (Exception e) {
      // Loggear el error de la API
      return Collections.emptyList();
    }
  }
}