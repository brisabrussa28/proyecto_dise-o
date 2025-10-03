package ar.edu.utn.frba.dds.domain.fuentes;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.lector.Lector;
import ar.edu.utn.frba.dds.domain.lector.configuracion.ConfiguracionLector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.persistence.*;

@Entity
@DiscriminatorValue("ESTATICA")
public class FuenteEstatica extends Fuente {

  private String rutaArchivo;

  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
  private ConfiguracionLector configuracionLector;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
  @JoinTable(name = "hecho_x_coleccion")
  private final List<Hecho> hechosPersistidos = new ArrayList<>();

  protected FuenteEstatica() {
    super();
  }

  public FuenteEstatica(String nombre, String rutaArchivo, ConfiguracionLector configLector) {
    super(nombre);
    if (rutaArchivo == null || rutaArchivo.isBlank()) {
      throw new IllegalArgumentException("La ruta del archivo no puede ser nula o vacía.");
    }
    if (configLector == null) {
      throw new IllegalArgumentException("La configuración del lector no puede ser nula.");
    }
    this.rutaArchivo = rutaArchivo;
    this.configuracionLector = configLector;

    // Se cargan los hechos al crear la instancia.
    this.cargarHechosDesdeArchivo();
  }

  /**
   * Metodo privado que encapsula la lógica de la lectura de hechos.
   */
  private void cargarHechosDesdeArchivo() {
    // El lector se crea como una variable local, solo cuando se necesita.
    Lector<Hecho> lector = this.configuracionLector.build(Hecho.class);
    List<Hecho> hechosImportados = lector.importar(this.rutaArchivo);

    this.hechosPersistidos.clear();
    this.hechosPersistidos.addAll(hechosImportados);
  }

  @Override
  public List<Hecho> obtenerHechos() {
    // Devuelve la lista que ya está en memoria y persistida, sin releer el archivo.
    return Collections.unmodifiableList(this.hechosPersistidos);
  }
}
