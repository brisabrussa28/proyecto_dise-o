package ar.edu.utn.frba.dds.domain.fuentes;

import ar.edu.utn.frba.dds.domain.exportador.Exportador;
import ar.edu.utn.frba.dds.domain.exportador.configuracion.ConfiguracionExportador;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.lector.Lector;
import ar.edu.utn.frba.dds.domain.lector.configuracion.ConfiguracionLector;

import java.util.Collections;
import java.util.List;
import javax.persistence.*;

/**
 * Clase abstracta para fuentes que usan una copia local como caché EN MEMORIA.
 * Persiste la configuración de su Lector y Exportador a través de entidades.
 */
@Entity
public abstract class FuenteDeCopiaLocal extends Fuente {

  protected String rutaCopiaLocal;

  @Transient // La caché de hechos es puramente en memoria, no se persiste.
  protected List<Hecho> cacheDeHechos;

  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
  private ConfiguracionLector configuracionLector;

  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
  private ConfiguracionExportador configuracionExportador;

  @Transient
  protected Lector<Hecho> lector;

  @Transient
  protected Exportador<Hecho> exportador;

  protected FuenteDeCopiaLocal() {
    super();
  }

  public FuenteDeCopiaLocal(
      String nombre,
      String rutaCopiaLocal,
      ConfiguracionLector configLector,
      ConfiguracionExportador configExportador
  ) {
    super(nombre);
    this.rutaCopiaLocal = rutaCopiaLocal;
    this.configuracionLector = configLector;
    this.configuracionExportador = configExportador;
    reconstruirDependencias(); // Reconstruye y carga la caché inicial
    this.cacheDeHechos = this.lector.importar(this.rutaCopiaLocal);
  }

  @PostLoad
  protected void reconstruirDependencias() {
    if (this.configuracionLector != null) {
      this.lector = this.configuracionLector.build(Hecho.class);
    }
    if (this.configuracionExportador != null) {
      this.exportador = this.configuracionExportador.build();
    }
  }

  protected abstract List<Hecho> consultarNuevosHechos();

  @Override
  public List<Hecho> obtenerHechos() {
    if (cacheDeHechos == null) {
      this.cacheDeHechos = this.lector.importar(this.rutaCopiaLocal);
    }
    return Collections.unmodifiableList(this.cacheDeHechos);
  }

  public void forzarActualizacionSincrona() {
    List<Hecho> nuevosHechos = this.consultarNuevosHechos();
    if (nuevosHechos != null && !nuevosHechos.isEmpty()) {
      this.cacheDeHechos = nuevosHechos;
      this.exportador.exportar(this.cacheDeHechos, this.rutaCopiaLocal);
    }
  }
}