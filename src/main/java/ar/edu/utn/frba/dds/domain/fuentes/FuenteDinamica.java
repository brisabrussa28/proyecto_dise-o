package ar.edu.utn.frba.dds.domain.fuentes;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.persistence.*;

@Entity
@DiscriminatorValue("DINAMICA")
public class FuenteDinamica extends Fuente { // Hereda de la base

  // Relación persistente: solo esta clase guarda Hechos en la BD.
  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
  @JoinColumn(name = "fuente_dinamica_id") // Buena práctica para OneToMany
  private List<Hecho> hechosPersistidos;

  protected FuenteDinamica() { super(); }

  public FuenteDinamica(String nombre) {
    super(nombre);
    this.hechosPersistidos = new ArrayList<>();
  }

  public void agregarHecho(Hecho hecho) {
    if (this.hechosPersistidos == null) {
      this.hechosPersistidos = new ArrayList<>();
    }
    this.hechosPersistidos.add(hecho);
  }

  @Override
  public List<Hecho> obtenerHechos() {
    return this.hechosPersistidos == null
           ? Collections.emptyList()
           : new ArrayList<>(this.hechosPersistidos);
  }
}