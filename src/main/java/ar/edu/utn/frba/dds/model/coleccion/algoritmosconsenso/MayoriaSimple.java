package ar.edu.utn.frba.dds.model.coleccion.algoritmosconsenso;

import ar.edu.utn.frba.dds.model.hecho.Hecho;
import java.util.List;
import java.util.Set;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("May_simple")
public class MayoriaSimple extends AlgoritmoDeConsenso {

  private long calcularUmbralDeMayoria(int totalFuentes) {
    return (long) Math.ceil((double) totalFuentes / 2);
  }

  @Override
  protected boolean esConsensuado(Hecho hecho, List<Set<Hecho>> hechosDeFuentes) {
    if (hechosDeFuentes.isEmpty()) return false;

    long umbralDeMayoria = calcularUmbralDeMayoria(hechosDeFuentes.size());

    // Usamos el nuevo método flexible
    long menciones = hechosDeFuentes.stream()
                                    .filter(setHechos -> existeHechoSimilarEnFuente(setHechos, hecho))
                                    .count();

    return menciones >= umbralDeMayoria;
  }
}