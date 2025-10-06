package ar.edu.utn.frba.dds.domain.fuentes;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;

/**
 * Fuente que combina los hechos de múltiples otras fuentes en tiempo real.
 * No mantiene una copia local, ya que las fuentes subyacentes ya son persistentes.
 */
@Entity
@DiscriminatorValue("AGREGACION")
public class FuenteDeAgregacion extends Fuente {

  /**
   * Relación con las fuentes que se van a agregar.
   * LAZY se usa para que las fuentes no se carguen de la BD hasta que se necesiten.
   */
  @OneToMany(fetch = FetchType.LAZY)
  @JoinColumn(name = "fuente_fuente_cargada")
  private List<Fuente> fuentesCargadas = new ArrayList<>();

  /**
   * Constructor vacío requerido por JPA.
   */
  protected FuenteDeAgregacion() {
    super();
  }

  public FuenteDeAgregacion(String nombre) {
    super(nombre);
  }

  /**
   * Agrega una fuente a la lista de fuentes que serán consultadas.
   *
   * @param fuente La fuente a agregar. No puede ser nula.
   */
  public void agregarFuente(Fuente fuente) {
    if (fuente == null) {
      throw new IllegalArgumentException("No se puede agregar una fuente nula.");
    }
    if (this.fuentesCargadas == null) {
      this.fuentesCargadas = new ArrayList<>();
    }
    this.fuentesCargadas.add(fuente);
  }

  /**
   * Obtiene una copia de la lista de fuentes que componen esta agregación.
   *
   * @return Una nueva lista conteniendo las fuentes.
   */
  public List<Fuente> getFuentesCargadas() {
    return new ArrayList<>(this.fuentesCargadas);
  }

  /**
   * Consulta los hechos de todas las fuentes cargadas, los combina en una sola
   * lista y elimina duplicados. Esta operación se realiza en tiempo real.
   *
   * @return La lista consolidada y sin duplicados de hechos.
   */
  @Override
  public List<Hecho> obtenerHechos() {
    if (this.fuentesCargadas == null || this.fuentesCargadas.isEmpty()) {
      return new ArrayList<>();
    }
    return this.fuentesCargadas.stream()
                               .flatMap(fuente -> fuente.obtenerHechos().stream())
                               .distinct()
                               .collect(Collectors.toList());
  }
}
