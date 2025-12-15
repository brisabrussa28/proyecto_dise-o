package ar.edu.utn.frba.dds.model.fuentes;

import ar.edu.utn.frba.dds.model.geolocalizacion.GeoApi;
import ar.edu.utn.frba.dds.model.hecho.EnriquecedorDeHechos;
import ar.edu.utn.frba.dds.model.hecho.Hecho;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;

/**
 * Clase abstracta para todas las fuentes que almacenan una lista local de hechos.
 */
@Entity
@DiscriminatorValue("CON_HECHOS")
public abstract class FuenteConHechos extends Fuente {

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  @JoinColumn(name = "fuente_id")
  protected List<Hecho> hechos = new ArrayList<>();

  protected FuenteConHechos() {
    super();
  }

  public FuenteConHechos(String nombre) {
    super(nombre);
  }

  /**
   * Implementación estándar para fuentes que tienen una lista local.
   * Devuelve una copia inmutable de la lista.
   */
  @Override
  public List<Hecho> getHechos() {
    if (this.hechos == null) {
      return Collections.emptyList();
    }
    return Collections.unmodifiableList(this.hechos);
  }

  /**
   * Implementación estándar del método de enriquecimiento para fuentes con lista local.
   */
  public void enriquecer(GeoApi geoApi) {
    Objects.requireNonNull(geoApi, "La GeoApi no puede ser nula para enriquecer los hechos.");
    if (this.hechos == null || this.hechos.isEmpty()) {
      return;
    }

    EnriquecedorDeHechos enriquecedor = new EnriquecedorDeHechos(geoApi);

    enriquecedor.completarAsincrono(new ArrayList<>(this.hechos))
                .thenAccept(hechosEnriquecidos -> {
                  this.hechos.clear();
                  this.hechos.addAll(hechosEnriquecidos);
                  System.out.println("Enriquecimiento en segundo plano completado para la fuente: " + this.getNombre());
                });
  }

  public void agregarHecho(Hecho hecho) {
    if (hecho == null) {
      throw new IllegalArgumentException("No se puede agregar un hecho nulo.");
    }
    if (this.hechos == null) {
      this.hechos = new ArrayList<>();
    }
    this.hechos.add(hecho);
  }

  /**
   * Setter para asignar hechos desde el repositorio.
   */
  public void setHechos(List<Hecho> hechos) {
    if (this.hechos == null) {
      this.hechos = new ArrayList<>();
    } else {
      this.hechos.clear();
    }
    if (hechos != null) {
      this.hechos.addAll(hechos);
    }
  }
}