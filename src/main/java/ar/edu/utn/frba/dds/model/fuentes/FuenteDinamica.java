package ar.edu.utn.frba.dds.model.fuentes;

import ar.edu.utn.frba.dds.model.hecho.Hecho;
import com.fasterxml.jackson.annotation.JsonProperty;
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

/**
 * Fuente dinámica que permite agregar hechos en tiempo de ejecución.
 * Los hechos se persisten en la base de datos con una foreign key específica.
 */
@Entity
@DiscriminatorValue("DINAMICA")
public class FuenteDinamica extends FuenteConHechos {

  @Transient
  @JsonProperty("tipo_fuente")
  private String tipo_fuente;

  // Relación persistente específica para FuenteDinamica
  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  @JoinColumn(name = "fuente_dinamica_id")
  private List<Hecho> hechosPersistidos = new ArrayList<>();

  protected FuenteDinamica() {
    super();
    this.tipo_fuente = "DINAMICA";
  }

  public FuenteDinamica(String nombre) {
    super(nombre);
    this.tipo_fuente = "DINAMICA";
  }

  /**
   * Agrega un hecho a esta fuente dinámica.
   */
  @Override
  public void agregarHecho(Hecho hecho) {
    if (hecho == null) {
      throw new IllegalArgumentException("No se puede agregar un hecho nulo.");
    }
    if (this.hechosPersistidos == null) {
      this.hechosPersistidos = new ArrayList<>();
    }
    this.hechosPersistidos.add(hecho);
  }

  /**
   * Obtiene todos los hechos de esta fuente.
   * Devuelve una lista inmutable.
   */
  @Override
  public List<Hecho> getHechos() {
    if (this.hechosPersistidos == null || this.hechosPersistidos.isEmpty()) {
      return Collections.emptyList();
    }
    return this.hechosPersistidos;
  }

  /**
   * Setter específico para hechos persistidos.
   * Usado por los repositorios al cargar desde la base de datos.
   */
  public void setHechosPersistidos(List<Hecho> hechos) {
    if (this.hechosPersistidos == null) {
      this.hechosPersistidos = new ArrayList<>();
    } else {
      this.hechosPersistidos.clear();
    }
    if (hechos != null) {
      this.hechosPersistidos.addAll(hechos);
    }
  }

  /**
   * Remueve un hecho de esta fuente.
   */
  public void removerHecho(Hecho hecho) {
    if (this.hechosPersistidos != null && hecho != null) {
      this.hechosPersistidos.removeIf(h -> h.getId() != null && h.getId().equals(hecho.getId()));
    }
  }

  /**
   * Limpia todos los hechos de esta fuente.
   */
  public void limpiarHechos() {
    if (this.hechosPersistidos != null) {
      this.hechosPersistidos.clear();
    }
  }

  /**
   * Verifica si esta fuente está vacía.
   */
  public boolean estaVacia() {
    return this.hechosPersistidos == null || this.hechosPersistidos.isEmpty();
  }

  public String getTipoFuente() {
    return this.tipo_fuente;
  }
}