package ar.edu.utn.frba.dds.model.fuentes;

import ar.edu.utn.frba.dds.domain.hecho.EnriquecedorDeHechos;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.lector.Lector;
import ar.edu.utn.frba.dds.domain.lector.configuracion.ConfiguracionLector;
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

@Entity
@DiscriminatorValue("ESTATICA")
public class FuenteEstatica extends FuenteConHechos {

  private String fuente_ruta_archivo;

//  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
  @Transient
  private ConfiguracionLector fuente_configuracion_lector;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
  @JoinColumn(name = "hecho_fuente")
  private final List<Hecho> hechosPersistidos = new ArrayList<>();

  protected FuenteEstatica() {
    super();
  }

  public FuenteEstatica(String nombre, String fuente_ruta_archivo, ConfiguracionLector configLector) {
    super(nombre);
    if (fuente_ruta_archivo == null || fuente_ruta_archivo.isBlank()) {
      throw new IllegalArgumentException("La ruta del archivo no puede ser nula o vacía.");
    }
    if (configLector == null) {
      throw new IllegalArgumentException("La configuración del lector no puede ser nula.");
    }
    this.fuente_ruta_archivo = fuente_ruta_archivo;
    this.fuente_configuracion_lector = configLector;

    // Se cargan los hechos al crear la instancia.
    this.cargarHechosDesdeArchivo();
  }

  /**
   * Metodo privado que encapsula la lógica de la lectura de hechos.
   */
  private void cargarHechosDesdeArchivo() {
    // El lector se crea como una variable local, solo cuando se necesita.
    Lector<Hecho> lector = this.fuente_configuracion_lector.build(Hecho.class);
    List<Hecho> hechosImportados = lector.importar(this.fuente_ruta_archivo);

    this.hechosPersistidos.clear();

    this.hechosPersistidos.addAll(hechosImportados);
  }

  @Override
  public List<Hecho> getHechos() {
    // Devuelve la lista que ya está en memoria y persistida, sin releer el archivo.
    return Collections.unmodifiableList(this.hechosPersistidos);
  }
}
