package ar.edu.utn.frba.dds.domain.fuentes;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.lector.Lector;
import ar.edu.utn.frba.dds.domain.lector.configuracion.ConfiguracionLector;
import java.util.List;
import javax.persistence.*;

@Entity
@DiscriminatorValue("ESTATICA")
public class FuenteEstatica extends Fuente {

  private String rutaArchivo;

  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
  private ConfiguracionLector configuracionLector;

  @Transient
  private Lector<Hecho> lector;

  protected FuenteEstatica() { super(); }

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