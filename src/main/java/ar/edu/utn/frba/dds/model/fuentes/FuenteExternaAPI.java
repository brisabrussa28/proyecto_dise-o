package ar.edu.utn.frba.dds.model.fuentes;

import ar.edu.utn.frba.dds.model.fuentes.apis.FuenteAdapter;
import ar.edu.utn.frba.dds.model.fuentes.apis.configuracion.ConfiguracionAdapter;
import ar.edu.utn.frba.dds.model.hecho.Hecho;
import java.util.List;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.PostLoad;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "fuente_externa_api")
@PrimaryKeyJoinColumn(name = "fuente_id")
@DiscriminatorValue("API_EXTERNA")
public class FuenteExternaAPI extends FuenteDeCopiaLocal {

  @Transient
  private FuenteAdapter adaptador;

  @Column(name = "api_url")
  private String urlBase;

  @Transient
  private ConfiguracionAdapter configuracionAdapter;

  public FuenteExternaAPI() {
    super();
  }

  public FuenteExternaAPI(String nombre, ConfiguracionAdapter configAdapter) {
    super(nombre);
    Objects.requireNonNull(configAdapter, "La configuración del adaptador no puede ser nula.");
    this.configuracionAdapter = configAdapter;
    this.reconstruirDependencias();
  }

  @Override
  public String getTipo() {
    return "API_EXTERNA";
  }

  @PostLoad
  protected void reconstruirDependencias() {
    // Nota: configuracionAdapter es transient, por lo que será null al cargar de DB.
    // Si se requiere persistencia real del adaptador, se debería guardar su configuración
    // (ej: clase del adaptador o parámetros) en columnas de la base de datos.
    if (this.configuracionAdapter != null) {
      this.adaptador = this.configuracionAdapter.build();
    }
  }

  @Override
  protected List<Hecho> consultarNuevosHechos() {
    if (this.adaptador == null) {
      // Intento de recuperación si solo tenemos la URL
      if (this.urlBase != null) {
        System.err.println("Advertencia: Adaptador nulo. Se tiene URL pero falta la estrategia de configuración.");
      }
      return List.of();
    }
    try {
      return this.adaptador.consultarHechos();
    } catch (Exception e) {
      System.err.println("Error al consultar API '" + this.getNombre() + "': " + e.getMessage());
      return List.of();
    }
  }

  public void actualizarDesdeAPI() {
    List<Hecho> nuevosHechos = this.consultarNuevosHechos();
    if (nuevosHechos != null && !nuevosHechos.isEmpty()) {
      this.setHechos(nuevosHechos);
    }
  }

  public void setUrlBase(String urlBase) {
    this.urlBase = urlBase;
  }

  public String getUrlBase() {
    return this.urlBase;
  }

  public void setAdaptador(FuenteAdapter adaptador) {
    this.adaptador = adaptador;
  }

  public void setConfiguracionAdapter(ConfiguracionAdapter configuracionAdapter) {
    this.configuracionAdapter = configuracionAdapter;
    if (configuracionAdapter != null) {
      this.adaptador = configuracionAdapter.build();
    }
  }

  @Override
  public String toString() {
    return String.format("FuenteExternaAPI{id=%d, nombre='%s', url='%s'}",
                         getId(), getNombre(), urlBase);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof FuenteExternaAPI)) return false;
    if (!super.equals(o)) return false;
    FuenteExternaAPI that = (FuenteExternaAPI) o;
    return Objects.equals(urlBase, that.urlBase);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), urlBase);
  }
}