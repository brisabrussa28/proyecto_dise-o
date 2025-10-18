package ar.edu.utn.frba.dds.model.fuentes;

import ar.edu.utn.frba.dds.model.hecho.Hecho;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Clase abstracta para fuentes que mantienen una copia local persistida en la base de datos.
 * Las subclases deben implementar la lógica para consultar los datos frescos.
 */

//Esta deberia estar persistida.

//@Entity
public abstract class FuenteDeCopiaLocal extends FuenteConHechos {

  //@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
  private List<Hecho> copiaLocalDeHechos = new ArrayList<>();

  protected FuenteDeCopiaLocal() {
    super();
  }

  public FuenteDeCopiaLocal(String nombre) {
    super(nombre);
  }

  /**
   * Las subclases deben implementar este método para definir de dónde
   * obtienen la información actualizada (ej: una API, otra fuente, etc.).
   *
   * @return Una lista con los hechos más recientes.
   */
  protected abstract List<Hecho> consultarNuevosHechos();

  /**
   * Devuelve la copia local de los hechos que está persistida en la base de datos.
   * No consulta la fuente original para obtener los datos.
   *
   * @return Una lista inmutable de los hechos cacheados.
   */
  @Override
  public List<Hecho> getHechos() {
    return Collections.unmodifiableList(this.copiaLocalDeHechos);
  }

  /**
   * Ejecuta la lógica para buscar nuevos hechos y actualiza la copia local
   * persistida en la base de datos.
   */
  public void forzarActualizacionSincrona() {
    List<Hecho> nuevosHechos = this.consultarNuevosHechos();
    if (nuevosHechos != null && !nuevosHechos.isEmpty()) {
      this.copiaLocalDeHechos.clear();
      this.copiaLocalDeHechos.addAll(nuevosHechos);
    }
  }
}

