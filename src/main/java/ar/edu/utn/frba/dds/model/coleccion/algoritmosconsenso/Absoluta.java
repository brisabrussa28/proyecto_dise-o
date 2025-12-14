package ar.edu.utn.frba.dds.model.coleccion.algoritmosconsenso;

import ar.edu.utn.frba.dds.model.hecho.Hecho;
import java.util.List;
import java.util.Set;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("Absoluta")
public class Absoluta extends AlgoritmoDeConsenso {

  @Override
  protected boolean esConsensuado(Hecho hecho, List<Set<Hecho>> hechosDeFuentes) {
    if (hechosDeFuentes.isEmpty()) return false;

    // Usamos el nuevo método flexible
    return hechosDeFuentes.stream()
                          .allMatch(setHechos -> existeHechoSimilarEnFuente(setHechos, hecho));
  }
}