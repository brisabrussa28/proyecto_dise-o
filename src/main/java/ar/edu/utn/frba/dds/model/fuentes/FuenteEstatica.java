package ar.edu.utn.frba.dds.model.fuentes;

import ar.edu.utn.frba.dds.model.hecho.Hecho;
import ar.edu.utn.frba.dds.model.lector.Lector;
import ar.edu.utn.frba.dds.model.lector.configuracion.ConfiguracionLector;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

/**
 * Fuente estática que carga hechos desde un archivo.
 * Los hechos se cargan una vez y se persisten en la base de datos.
 */
@Entity
@DiscriminatorValue("ESTATICA")
public class FuenteEstatica extends FuenteConHechos {

  @Transient
  private String fuente_ruta_archivo;

  @Transient
  private ConfiguracionLector fuente_configuracion_lector;

  // Relación persistente específica para FuenteEstatica
  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  @JoinColumn(name = "fuente_estatica_id")
  private List<Hecho> hechosPersistidos = new ArrayList<>();

  public FuenteEstatica() {
    super();
  }

  /**
   * Crea una FuenteEstatica que carga hechos desde un archivo.
   * Los hechos se cargan inmediatamente al crear la fuente.
   */
  public FuenteEstatica(String nombre, ConfiguracionLector configLector) {
    super(nombre);
    if (configLector == null) {
      throw new IllegalArgumentException("La configuración del lector no puede ser nula.");
    }
    this.fuente_configuracion_lector = configLector;
    this.cargarHechosDesdeArchivo();
  }

  /**
   * Crea una FuenteEstatica a partir de hechos ya leídos.
   * Este constructor se usa cuando los hechos ya fueron procesados.
   */
  public FuenteEstatica(String nombre, List<Hecho> hechosImportados) {
    super(nombre);
    if (hechosImportados == null) {
      throw new IllegalArgumentException("La lista de hechos no puede ser nula.");
    }
    this.fuente_ruta_archivo = null;
    this.fuente_configuracion_lector = null;
    this.hechosPersistidos.clear();
    this.hechosPersistidos.addAll(hechosImportados);
  }

  /**
   * Método privado que encapsula la lógica de la lectura de hechos desde archivo.
   */
  private void cargarHechosDesdeArchivo() {
    if (this.fuente_configuracion_lector == null) {
      return;
    }
    try {
      Lector<Hecho> lector = this.fuente_configuracion_lector.build(Hecho.class);
      List<Hecho> hechosImportados = lector.importar(this.fuente_ruta_archivo);

      this.hechosPersistidos.clear();
      if (hechosImportados != null) {
        this.hechosPersistidos.addAll(hechosImportados);
      }
    } catch (Exception e) {
      System.err.println("Error al cargar hechos desde archivo para la fuente '" +
                             this.fuente_nombre + "': " + e.getMessage());
      this.hechosPersistidos.clear();
    }
  }

  /**
   * Obtiene todos los hechos de esta fuente.
   * Devuelve una lista inmutable.
   */
  @Override
  public List<Hecho> getHechos() {
    if (this.hechosPersistidos == null || this.hechosPersistidos.isEmpty()) {
      return Collections.emptyList();
    }
    return Collections.unmodifiableList(this.hechosPersistidos);
  }

  /**
   * Setter específico para hechos persistidos.
   * Usado por los repositorios al cargar desde la base de datos.
   */
  public void setHechosPersistidos(List<Hecho> hechos) {
    if (this.hechosPersistidos == null) {
      this.hechosPersistidos = new ArrayList<>();
    } else {
      this.hechosPersistidos.clear();
    }
    if (hechos != null) {
      this.hechosPersistidos.addAll(hechos);
    }
  }

  /**
   * Recarga los hechos desde el archivo.
   * Útil si el archivo fuente ha sido modificado.
   */
  public void recargarHechos() {
    this.cargarHechosDesdeArchivo();
  }

  /**
   * Verifica si esta fuente está vacía.
   */
  public boolean estaVacia() {
    return this.hechosPersistidos == null || this.hechosPersistidos.isEmpty();
  }

  /**
   * Obtiene la ruta del archivo de origen (si existe).
   */
  public String getRutaArchivo() {
    return this.fuente_ruta_archivo;
  }

  /**
   * Establece la ruta del archivo de origen.
   */
  public void setRutaArchivo(String ruta) {
    this.fuente_ruta_archivo = ruta;
  }

  /**
   * Obtiene la configuración del lector (si existe).
   */
  public ConfiguracionLector getConfiguracionLector() {
    return this.fuente_configuracion_lector;
  }

  /**
   * Establece la configuración del lector.
   */
  public void setConfiguracionLector(ConfiguracionLector config) {
    this.fuente_configuracion_lector = config;
  }
}