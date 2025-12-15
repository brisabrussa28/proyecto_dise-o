package ar.edu.utn.frba.dds.model.fuentes;

import ar.edu.utn.frba.dds.model.hecho.Hecho;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

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
  @ManyToMany
  @JoinTable(
      name = "fuente_agregacion",
      joinColumns = @JoinColumn(name = "padre_id"),
      inverseJoinColumns = @JoinColumn(name = "hijo_id")
  )
  private List<Fuente> fuentesCargadas = new ArrayList<>();

  /**
   * Constructor vacío requerido por JPA.
   */
  public FuenteDeAgregacion() {
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

  // --- NUEVO MÉTODO PARA BORRADO SEGURO ---
  public void removerFuente(Fuente fuente) {
    if (this.fuentesCargadas != null) {
      // Usamos removeIf para evitar problemas de concurrencia al iterar
      this.fuentesCargadas.removeIf(f -> f.getId().equals(fuente.getId()));
    }
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
  public List<Hecho> getHechos() {
    if (this.fuentesCargadas == null || this.fuentesCargadas.isEmpty()) {
      return new ArrayList<>();
    }

    return this.fuentesCargadas.stream()
                               .map(Fuente::getHechos)
                               .flatMap(List::stream)
                               .distinct()
                               .collect(Collectors.toList());
  }
}