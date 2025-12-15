package ar.edu.utn.frba.dds.model.fuentes;

import ar.edu.utn.frba.dds.model.fuentes.apis.FuenteAdapter;
import ar.edu.utn.frba.dds.model.fuentes.apis.configuracion.ConfiguracionAdapter;
import ar.edu.utn.frba.dds.model.hecho.Hecho;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.PostLoad;
import javax.persistence.Transient;

@Entity
@DiscriminatorValue("API_EXTERNA")
public class FuenteExternaAPI extends FuenteDeCopiaLocal {

  @Transient
  private FuenteAdapter adaptador;

  @Column(name = "api_url")
  private String urlBase;

  @Transient
  private ConfiguracionAdapter configuracionAdapter;

  // Relación persistente específica para FuenteExternaAPI
  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  @JoinColumn(name = "fuente_externa_api_id")
  private List<Hecho> copiaLocalDeHechos = new ArrayList<>();

  public FuenteExternaAPI() {
    super();
  }

  public FuenteExternaAPI(String nombre, ConfiguracionAdapter configAdapter) {
    super(nombre);
    if (configAdapter == null) {
      throw new IllegalArgumentException("La configuración del adaptador no puede ser nula.");
    }
    this.configuracionAdapter = configAdapter;
    this.reconstruirDependencias();
  }

  @PostLoad
  protected void reconstruirDependencias() {
    if (this.configuracionAdapter != null) {
      this.adaptador = this.configuracionAdapter.build();
    }
  }

  @Override
  protected List<Hecho> consultarNuevosHechos() {
    if (this.adaptador == null) {
      System.err.println("Advertencia: El adaptador para la fuente '" + this.fuente_nombre + "' no está inicializado.");
      return Collections.emptyList();
    }
    try {
      return this.adaptador.consultarHechos();
    } catch (Exception e) {
      System.err.println("Error al consultar la API externa para la fuente '" + this.fuente_nombre + "': " + e.getMessage());
      return Collections.emptyList();
    }
  }

  @Override
  public List<Hecho> getHechos() {
    if (this.copiaLocalDeHechos == null || this.copiaLocalDeHechos.isEmpty()) {
      return Collections.emptyList();
    }
    return Collections.unmodifiableList(this.copiaLocalDeHechos);
  }

  @Override
  public void forzarActualizacionSincrona() {
    List<Hecho> nuevosHechos = this.consultarNuevosHechos();
    if (nuevosHechos != null && !nuevosHechos.isEmpty()) {
      this.copiaLocalDeHechos.clear();
      this.copiaLocalDeHechos.addAll(nuevosHechos);
    }
  }

  /**
   * Setter para la copia local de hechos.
   */
  @Override
  public void setCopiaLocalDeHechos(List<Hecho> hechos) {
    if (this.copiaLocalDeHechos == null) {
      this.copiaLocalDeHechos = new ArrayList<>();
    } else {
      this.copiaLocalDeHechos.clear();
    }
    if (hechos != null) {
      this.copiaLocalDeHechos.addAll(hechos);
    }
  }

  public void setUrlBase(String urlBase) {
    this.urlBase = urlBase;
  }

  public String getUrlBase() {
    return this.urlBase;
  }
}