package ar.edu.utn.frba.dds.model.fuentes;

import ar.edu.utn.frba.dds.model.hecho.Hecho;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

/**
 * Fuente que combina los hechos de múltiples otras fuentes en tiempo real.
 */
@Entity
@Table(name = "fuente_agregacion")
@PrimaryKeyJoinColumn(name = "fuente_id")
@DiscriminatorValue("AGREGACION")
public class FuenteDeAgregacion extends Fuente {

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "fuente_agregacion_relacion",
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

  @Override
  public String getTipo() {
    return "AGREGACION";
  }

  public void agregarFuente(Fuente fuente) {
    Objects.requireNonNull(fuente, "No se puede agregar una fuente nula.");

    // FIXED: Prevenir recursión directa (agregarse a sí mismo)
    if (Objects.equals(fuente.getId(), this.getId())) {
      throw new IllegalArgumentException("Una fuente no puede agregarse a sí misma.");
    }

    if (this.fuentesCargadas == null) {
      this.fuentesCargadas = new ArrayList<>();
    }

    // Verificar duplicados
    boolean yaExiste = this.fuentesCargadas.stream()
                                           .anyMatch(f -> Objects.equals(f.getId(), fuente.getId()));

    if (!yaExiste) {
      this.fuentesCargadas.add(fuente);
    }
  }

  public void removerFuente(Fuente fuente) {
    if (this.fuentesCargadas != null && fuente != null) {
      this.fuentesCargadas.removeIf(f -> Objects.equals(f.getId(), fuente.getId()));
    }
  }

  public List<Fuente> getFuentesCargadas() {
    if (this.fuentesCargadas == null) {
      return new ArrayList<>();
    }
    return new ArrayList<>(this.fuentesCargadas);
  }

  @Override
  public List<Hecho> getHechos() {
    if (this.fuentesCargadas == null || this.fuentesCargadas.isEmpty()) {
      return new ArrayList<>();
    }

    return this.fuentesCargadas.stream()
                               .filter(Objects::nonNull)
                               .map(Fuente::getHechos)
                               .flatMap(List::stream)
                               .distinct() // Evita hechos duplicados si una fuente está agregada múltiples veces indirectamente
                               .collect(Collectors.toList());
  }

  public void setFuentesCargadas(List<Fuente> fuentes) {
    if (this.fuentesCargadas == null) {
      this.fuentesCargadas = new ArrayList<>();
    } else {
      this.fuentesCargadas.clear();
    }

    if (fuentes != null) {
      for (Fuente f : fuentes) {
        if (f != null && !Objects.equals(f.getId(), this.getId())) {
          this.agregarFuente(f); // Usar agregarFuente para validar duplicados
        }
      }
    }
  }

  public int cantidadFuentes() {
    return this.fuentesCargadas == null ? 0 : this.fuentesCargadas.size();
  }

  @Override
  public String toString() {
    // FIXED: Eliminado getCantidadHechos() para evitar recursión infinita o costo excesivo
    return String.format("FuenteDeAgregacion{id=%d, nombre='%s', fuentesAgregadas=%d}",
                         getId(), getNombre(), cantidadFuentes());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof FuenteDeAgregacion)) return false;
    if (!super.equals(o)) return false;
    // La igualdad basada en listas mutables es peligrosa en JPA, mejor confiar en ID del padre
    return true;
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }
}