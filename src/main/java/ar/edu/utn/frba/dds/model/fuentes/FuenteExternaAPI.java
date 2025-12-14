package ar.edu.utn.frba.dds.model.fuentes;

import ar.edu.utn.frba.dds.model.fuentes.apis.FuenteAdapter;
import ar.edu.utn.frba.dds.model.fuentes.apis.configuracion.ConfiguracionAdapter;
import ar.edu.utn.frba.dds.model.hecho.Hecho;
import ch.qos.logback.core.net.server.Client;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
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

  public FuenteExternaAPI() {
    super();
  }

  public FuenteExternaAPI(String nombre, ConfiguracionAdapter configAdapter) {
    super(nombre);
    if (configAdapter == null) {
      throw new IllegalArgumentException("La configuración del adaptador no puede ser nula.");
    }
    this.configuracionAdapter = configAdapter;
    this.reconstruirDependencias(); // Construye el adaptador al crear la instancia
  }

  /**
   * Reconstruye las dependencias transitorias (como el adaptador) después
   * de que la entidad es cargada desde la base de datos.
   */
  @PostLoad
  protected void reconstruirDependencias() {
    if (this.configuracionAdapter != null) {
      this.adaptador = this.configuracionAdapter.build();
    }
  }

  /**
   * Implementa la lógica para consultar los hechos desde la API externa
   * a través del adaptador configurado.
   *
   * @return Una lista de hechos obtenidos de la API, o una lista vacía si ocurre un error.
   */
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
      // Devolvemos una lista vacía para no borrar la copia local si la API falla.
      return Collections.emptyList();
    }
  }

  public void setUrlBase(String urlBase) {
    this.urlBase = urlBase;
  }
}
