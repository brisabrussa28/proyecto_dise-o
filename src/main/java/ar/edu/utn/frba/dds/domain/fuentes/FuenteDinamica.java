package ar.edu.utn.frba.dds.domain.fuentes;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.serializadores.Lector.Lector;
import ar.edu.utn.frba.dds.domain.serializadores.exportador.Exportador;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Entity;

/**
 * Clase fuente dinámica que permite agregar hechos en tiempo de ejecución.
 * Extiende FuenteDeCopiaLocal para soportar copias de seguridad a través de un Serializador.
 */
@Entity
public class FuenteDinamica extends FuenteDeCopiaLocal {

  /**
   * Constructor de la clase FuenteDinamica.
   *
   * @param nombre         Nombre de la fuente dinámica.
   * @param rutaCopiaLocal Ruta al archivo para las copias de seguridad.
   * @param lector         Lector para manejar la persistencia de la caché.
   * @param exportador     Exportador para guardar la caché.
   */
  public FuenteDinamica(String nombre, String rutaCopiaLocal, Lector<Hecho> lector, Exportador<Hecho> exportador) {
    // Llama al constructor de la clase padre (FuenteDeCopiaLocal)
    super(nombre, rutaCopiaLocal, lector, exportador);
    // La lista cargada por el serializador puede ser inmutable,
    // así que la envolvemos en un ArrayList para asegurar que podamos agregarle hechos.
    this.cacheDeHechos = new ArrayList<>(this.cacheDeHechos);
  }

  /**
   * Agrega un hecho a la fuente dinámica y persiste la lista actualizada.
   *
   * @param hecho Hecho a agregar a la fuente dinámica.
   */
  public void agregarHecho(Hecho hecho) {
    this.cacheDeHechos.add(hecho);
    // Guarda inmediatamente la copia local usando el serializador
    this.exportador.exportar(this.cacheDeHechos, this.rutaCopiaLocal);
  }

  /**
   * Implementación del método abstracto para consultar nuevos hechos.
   * Para FuenteDinamica, los "nuevos hechos" son simplemente los que ya tiene en caché,
   * ya que no consulta una fuente externa.
   *
   * @return Una copia de la lista actual de hechos.
   */
  @Override
  protected List<Hecho> consultarNuevosHechos() {
    // Este método es llamado por forzarActualizacionSincrona, que no es típicamente
    // usado por FuenteDinamica, pero se implementa por la herencia.
    // Simplemente devuelve el estado actual.
    return new ArrayList<>(this.cacheDeHechos);
  }
}