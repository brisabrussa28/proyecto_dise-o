package ar.edu.utn.frba.dds.model.fuentes;

import ar.edu.utn.frba.dds.model.hecho.Hecho;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

/**
 * Fuente que combina los hechos de múltiples otras fuentes en tiempo real.
 * No mantiene una copia local, ya que las fuentes subyacentes ya son persistentes.
 */
@Entity
@DiscriminatorValue("AGREGACION")
public class FuenteDeAgregacion extends Fuente {

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "fuente_agregacion",
      joinColumns = @JoinColumn(name = "padre_id"),
      inverseJoinColumns = @JoinColumn(name = "hijo_id")
  )
  private List<Fuente> fuentesCargadas = new ArrayList<>();

  public FuenteDeAgregacion() {
    super();
  }

  public FuenteDeAgregacion(String nombre) {
    super(nombre);
  }

  /**
   * Agrega una fuente a la lista de fuentes cargadas.
   */
  public void agregarFuente(Fuente fuente) {
    if (fuente == null) {
      throw new IllegalArgumentException("No se puede agregar una fuente nula.");
    }
    if (fuente.getId() != null && fuente.getId().equals(this.getId())) {
      throw new IllegalArgumentException("Una fuente no puede agregarse a sí misma.");
    }
    if (this.fuentesCargadas == null) {
      this.fuentesCargadas = new ArrayList<>();
    }
    // Evitar duplicados
    if (this.fuentesCargadas.stream().noneMatch(f -> f.getId().equals(fuente.getId()))) {
      this.fuentesCargadas.add(fuente);
    }
  }

  /**
   * Remueve una fuente de la lista de fuentes cargadas.
   */
  public void removerFuente(Fuente fuente) {
    if (this.fuentesCargadas != null && fuente != null) {
      this.fuentesCargadas.removeIf(f -> f.getId().equals(fuente.getId()));
    }
  }

  /**
   * Obtiene una copia de las fuentes cargadas.
   */
  public List<Fuente> getFuentesCargadas() {
    if (this.fuentesCargadas == null) {
      return new ArrayList<>();
    }
    return new ArrayList<>(this.fuentesCargadas);
  }

  /**
   * Obtiene todos los hechos de todas las fuentes agregadas.
   * Los hechos se obtienen en tiempo real de cada fuente.
   */
  @Override
  public List<Hecho> getHechos() {
    if (this.fuentesCargadas == null || this.fuentesCargadas.isEmpty()) {
      return new ArrayList<>();
    }

    return this.fuentesCargadas.stream()
                               .filter(f -> f != null)
                               .map(Fuente::getHechos)
                               .flatMap(List::stream)
                               .distinct()
                               .collect(Collectors.toList());
  }

  /**
   * Setter para fuentes cargadas.
   */
  public void setFuentesCargadas(List<Fuente> fuentes) {
    if (this.fuentesCargadas == null) {
      this.fuentesCargadas = new ArrayList<>();
    } else {
      this.fuentesCargadas.clear();
    }
    if (fuentes != null) {
      this.fuentesCargadas.addAll(fuentes);
    }
  }

  /**
   * Verifica si esta fuente de agregación está vacía.
   */
  public boolean isEmpty() {
    return this.fuentesCargadas == null || this.fuentesCargadas.isEmpty();
  }

  /**
   * Obtiene la cantidad de fuentes agregadas.
   */
  public int cantidadFuentes() {
    return this.fuentesCargadas == null ? 0 : this.fuentesCargadas.size();
  }
}