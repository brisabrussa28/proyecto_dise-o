package ar.edu.utn.frba.dds.model.fuentes;

import ar.edu.utn.frba.dds.model.geolocalizacion.GeoApi;
import ar.edu.utn.frba.dds.model.hecho.EnriquecedorDeHechos;
import ar.edu.utn.frba.dds.model.hecho.Hecho;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;

/**
 * Clase abstracta para todas las fuentes que almacenan una lista local de hechos.
 */
@Entity
public abstract class FuenteConHechos extends Fuente {

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
  @JoinColumn(name = "hecho_fuente")
  protected Set<Hecho> hechos = new HashSet<>();

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
    return Collections.unmodifiableList(new ArrayList<>(this.hechos));
  }

  /**
   * Implementación estándar del método de enriquecimiento para fuentes con lista local.
   */
  public void enriquecer(GeoApi geoApi) {
    Objects.requireNonNull(geoApi, "La GeoApi no puede ser nula para enriquecer los hechos.");
    if (this.hechos == null || this.hechos.isEmpty()) {
      return; // No hay nada que enriquecer.
    }

    EnriquecedorDeHechos enriquecedor = new EnriquecedorDeHechos(geoApi);

    // Llama al método no bloqueante y define un callback (thenAccept)
    // para actualizar la lista de la fuente con el nuevo resultado cuando el trabajo termine.
    enriquecedor.completarAsincrono(new ArrayList<>(this.hechos))
                .thenAccept(hechosEnriquecidos -> {
                  // Este bloque se ejecuta en un hilo de fondo cuando la API responde.
                  this.hechos.clear();
                  this.hechos.addAll(hechosEnriquecidos);
                  System.out.println("Enriquecimiento en segundo plano completado para la fuente: " + this.getNombre());
                });
  }

  public void agregarHecho(Hecho hecho) {
    this.hechos.add(hecho);
  }
}
