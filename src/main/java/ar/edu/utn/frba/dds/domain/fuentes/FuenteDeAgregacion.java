package ar.edu.utn.frba.dds.domain.fuentes;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.serializadores.Lector.Lector;
import ar.edu.utn.frba.dds.domain.serializadores.exportador.Exportador;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;


/**
 * Fuente que combina los hechos de múltiples otras fuentes en una sola lista.
 * También hereda de FuenteDeCopiaLocal para cachear el resultado agregado.
 */
@Entity
@DiscriminatorValue("AGREGACION")
public class FuenteDeAgregacion extends FuenteDeCopiaLocal {

  // Se anota la relación para que JPA la persista.
  // EAGER para que las fuentes se carguen al traer la FuenteDeAgregacion.
  @ManyToMany(fetch = FetchType.EAGER)
  private List<Fuente> fuentesCargadas; // No puede ser 'final'.

  // Constructor requerido por JPA.
  protected FuenteDeAgregacion() {
    super();
  }

  /**
   * Constructor de la fuente de agregación.
   *
   * @param nombre     Nombre de la fuente.
   * @param rutaCopia  Ruta del archivo para la copia local (caché).
   * @param lector     Lector para manejar la persistencia de la caché.
   * @param exportador Exportador para guardar la caché.
   */
  public FuenteDeAgregacion(String nombre, String rutaCopia, Lector<Hecho> lector, Exportador<Hecho> exportador) {
    super(nombre, rutaCopia, lector, exportador);
    this.fuentesCargadas = new ArrayList<>();
  }

  /**
   * Agrega una fuente a la lista de fuentes que serán agregadas.
   *
   * @param fuente La fuente a agregar.
   */
  public void agregarFuente(Fuente fuente) {
    if (this.fuentesCargadas == null) {
      this.fuentesCargadas = new ArrayList<>();
    }
    this.fuentesCargadas.add(fuente);
  }

  /**
   * Obtiene una copia de la lista de fuentes cargadas.
   *
   * @return Una nueva lista conteniendo las fuentes.
   */
  public List<Fuente> getFuentesCargadas() {
    return new ArrayList<>(this.fuentesCargadas);
  }

  /**
   * Consulta los hechos de todas las fuentes cargadas, los combina
   * en una sola lista y elimina duplicados.
   *
   * @return La lista consolidada de hechos.
   */
  @Override
  protected List<Hecho> consultarNuevosHechos() {
    if (this.fuentesCargadas == null || this.fuentesCargadas.isEmpty()) {
      return new ArrayList<>();
    }
    return this.fuentesCargadas.stream()
                               .flatMap(fuente -> fuente.obtenerHechos()
                                                        .stream())
                               .distinct()
                               .collect(Collectors.toList());
  }
}
