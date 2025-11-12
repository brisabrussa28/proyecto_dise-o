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

@Entity
@DiscriminatorValue("DINAMICA")
public class FuenteDinamica extends FuenteConHechos {

  @Transient
  @JsonProperty("tipo_fuente")
  private String tipo_fuente;

  // Relación persistente: solo esta clase guarda Hechos en la BD.
  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
  @JoinColumn(name = "hecho_fuente")
  private List<Hecho> hechosPersistidos;

  protected FuenteDinamica() {
    super();
    this.tipo_fuente = "DINAMICA";
  }

  public FuenteDinamica(String nombre) {
    super(nombre);
    this.hechosPersistidos = new ArrayList<>();
    this.tipo_fuente = "DINAMICA";
  }

  public void agregarHecho(Hecho hecho) {
    if (hecho == null) {
      throw new IllegalArgumentException("No se puede agregar un hecho nulo.");
    }
    if (this.hechosPersistidos == null) {
      this.hechosPersistidos = new ArrayList<>();
    }
    this.hechosPersistidos.add(hecho);
  }

  @Override
  public List<Hecho> getHechos() {
    return this.hechosPersistidos == null
           ? Collections.emptyList()
           : Collections.unmodifiableList(this.hechosPersistidos);
  }
}