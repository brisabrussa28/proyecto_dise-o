package ar.edu.utn.frba.dds.model.fuentes;

import ar.edu.utn.frba.dds.model.hecho.Hecho;
import ar.edu.utn.frba.dds.model.lector.Lector;
import ar.edu.utn.frba.dds.model.lector.configuracion.ConfiguracionLector;
import java.util.List;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * Fuente estática que carga hechos desde un archivo.
 */
@Entity
@Table(name = "fuente_estatica")
@PrimaryKeyJoinColumn(name = "fuente_id")
@DiscriminatorValue("ESTATICA")
public class FuenteEstatica extends FuenteConHechos {

  // FIXED: Cambiado de @Transient a @Column.
  // Si esto era transient, perdías la ruta al reiniciar la app.
  @Column(name = "ruta_archivo")
  private String fuenteRutaArchivo;

  // Este se mantiene Transient porque es configuración lógica,
  // pero deberías considerar si necesitas reconstruirlo al cargar.
  @Transient
  private ConfiguracionLector fuenteConfiguracionLector;

  public FuenteEstatica() {
    super();
  }

  public FuenteEstatica(String nombre, ConfiguracionLector configLector) {
    super(nombre);
    Objects.requireNonNull(configLector, "La configuración del lector no puede ser nula.");
    this.fuenteConfiguracionLector = configLector;
    // No cargamos inmediatamente en el constructor para no bloquear,
    // pero guardamos el estado.
  }

  public FuenteEstatica(String nombre, List<Hecho> hechosImportados) {
    super(nombre);
    Objects.requireNonNull(hechosImportados, "La lista de hechos no puede ser nula.");
    this.fuenteRutaArchivo = null;
    this.fuenteConfiguracionLector = null;
    this.setHechos(hechosImportados);
  }

  @Override
  public String getTipo() {
    return "ESTATICA";
  }

  /**
   * Intenta cargar los hechos. Retorna true si tuvo éxito.
   */
  public boolean cargarHechosDesdeArchivo() {
    if (this.fuenteConfiguracionLector == null || this.fuenteRutaArchivo == null) {
      return false;
    }

    try {
      Lector<Hecho> lector = this.fuenteConfiguracionLector.build(Hecho.class);
      List<Hecho> hechosImportados = lector.importar(this.fuenteRutaArchivo);

      if (hechosImportados != null && !hechosImportados.isEmpty()) {
        this.setHechos(hechosImportados);
        return true;
      }
    } catch (Exception e) {
      System.err.println("Error al cargar hechos desde archivo para la fuente '" +
                             this.getNombre() + "': " + e.getMessage());
      // No limpiamos los hechos anteriores en caso de error para no perder datos viejos si falla la lectura
    }
    return false;
  }

  public void recargarHechos() {
    this.cargarHechosDesdeArchivo();
  }

  public boolean estaVacia() {
    return this.hechos == null || this.hechos.isEmpty();
  }

  public String getRutaArchivo() {
    return this.fuenteRutaArchivo;
  }

  public void setRutaArchivo(String ruta) {
    this.fuenteRutaArchivo = ruta;
    // Si ya tenemos configuración, intentamos recargar
    if (ruta != null && this.fuenteConfiguracionLector != null) {
      this.cargarHechosDesdeArchivo();
    }
  }

  public ConfiguracionLector getConfiguracionLector() {
    return this.fuenteConfiguracionLector;
  }

  public void setConfiguracionLector(ConfiguracionLector config) {
    this.fuenteConfiguracionLector = config;
    if (config != null && this.fuenteRutaArchivo != null) {
      this.cargarHechosDesdeArchivo();
    }
  }

  @Override
  public String toString() {
    return String.format("FuenteEstatica{id=%d, nombre='%s', ruta='%s'}",
                         getId(), getNombre(), fuenteRutaArchivo);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof FuenteEstatica)) return false;
    if (!super.equals(o)) return false;
    FuenteEstatica that = (FuenteEstatica) o;
    return Objects.equals(fuenteRutaArchivo, that.fuenteRutaArchivo);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), fuenteRutaArchivo);
  }
}