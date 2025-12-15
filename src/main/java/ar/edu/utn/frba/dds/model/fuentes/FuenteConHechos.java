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
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

/**
 * Clase abstracta para todas las fuentes que almacenan una lista local de hechos.
 */
@Entity
@Table(name = "fuente_con_hechos")
@PrimaryKeyJoinColumn(name = "fuente_id")
@DiscriminatorValue("CON_HECHOS")
public abstract class FuenteConHechos extends Fuente {

  protected FuenteConHechos() {
    super();
  }

  public FuenteConHechos(String nombre) {
    super(nombre);
  }

  // FIXED: Cambiado a LAZY para evitar problemas de rendimiento (N+1)
  // FIXED: Usamos Set para evitar duplicados a nivel de persistencia si no hay orden
  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  @JoinColumn(name = "fuente_id")
  protected Set<Hecho> hechos = new HashSet<>();


  @Override
  public String getTipo(){
    return "CON_HECHOS";
  }

  /**
   * Implementación estándar para fuentes que tienen una lista local.
   */
  @Override
  public List<Hecho> getHechos() {
    if (this.hechos == null) {
      return Collections.emptyList();
    }
    // Convertimos el Set a List para mantener compatibilidad con la interfaz
    return Collections.unmodifiableList(new ArrayList<>(this.hechos));
  }

  public void enriquecer(GeoApi geoApi) {
    Objects.requireNonNull(geoApi, "La GeoApi no puede ser nula para enriquecer los hechos.");
    // Chequeo de nulidad seguro para Hibernate
    if (this.hechos == null || this.hechos.isEmpty()) {
      return;
    }

    EnriquecedorDeHechos enriquecedor = new EnriquecedorDeHechos(geoApi);

    // Copiamos a una lista para el procesamiento
    List<Hecho> listaParaProcesar = new ArrayList<>(this.hechos);

    enriquecedor.completarAsincrono(listaParaProcesar)
                .thenAccept(hechosEnriquecidos -> {
                  // Sincronizamos para modificar la colección gestionada
                  synchronized (this) {
                    this.hechos.clear();
                    this.hechos.addAll(hechosEnriquecidos);
                  }
                  System.out.println("Enriquecimiento en segundo plano completado para la fuente: " + this.getNombre());
                });
  }

  public void agregarHecho(Hecho hecho) {
    if (hecho == null) {
      throw new IllegalArgumentException("No se puede agregar un hecho nulo.");
    }
    if (this.hechos == null) {
      this.hechos = new HashSet<>();
    }

    this.hechos.add(hecho);
  }

  public void setHechos(List<Hecho> hechos) {
    if (this.hechos == null) {
      this.hechos = new HashSet<>();
    } else {
      this.hechos.clear();
    }
    if (hechos != null) {
      this.hechos.addAll(hechos);
    }
  }
}