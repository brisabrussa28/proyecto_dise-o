package ar.edu.utn.frba.dds.domain.fuentes;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.lector.Lector;
import ar.edu.utn.frba.dds.domain.lector.configuracion.ConfiguracionLector;
import java.util.List;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.PostLoad;
import javax.persistence.Transient;

@Entity
@DiscriminatorValue("ESTATICA")
public class FuenteEstatica extends Fuente {

  @Transient
  private String rutaArchivo;

  @Transient
//  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
  private ConfiguracionLector configuracionLector;

  @Transient
  private Lector<Hecho> lector;

  protected FuenteEstatica() {
    super();
  }

  public FuenteEstatica(String nombre, String rutaArchivo, ConfiguracionLector configLector) {
    super(nombre);
    this.rutaArchivo = rutaArchivo;
    this.configuracionLector = configLector;
    reconstruirDependencias();
  }

  @PostLoad
  protected void reconstruirDependencias() {
    if (this.configuracionLector != null) {
      this.lector = this.configuracionLector.build(Hecho.class);
    }
  }

  @Override
  public List<Hecho> obtenerHechos() {
    if (this.lector == null) {
      throw new IllegalStateException("El lector no fue inicializado.");
    }
    return lector.importar(rutaArchivo);
  }
}