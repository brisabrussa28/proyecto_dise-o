package ar.edu.utn.frba.dds.model.coleccion.algoritmosconsenso;

import ar.edu.utn.frba.dds.model.hecho.Hecho;
import java.util.List;
import java.util.Set;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("Verdadero")
public class Verdadero extends AlgoritmoDeConsenso {
  @Override
  protected boolean esConsensuado(Hecho hecho, List<Set<Hecho>> hechosDeFuentes) {
    return true;
  }
}
