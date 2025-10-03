package ar.edu.utn.frba.dds.domain.coleccion.algoritmosconsenso;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.List;
import java.util.Set;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;


/*
* MAYORIA SIMPLE
* si al menos la mitad de las fuentes del nodo contienen el mismo hecho,
* se lo considera consensuado;
 */

@Entity
@DiscriminatorValue("May_simple")
public class MayoriaSimple extends AlgoritmoDeConsenso {

  /**
   * Calcula el número mínimo de menciones necesarias para alcanzar la mayoría simple.
   */
  private long calcularUmbralDeMayoria(int totalFuentes) {
    return (long) Math.ceil((double) totalFuentes / 2);
  }

  /**
   * Verifica si el número de menciones de un hecho alcanza el umbral.
   */
  @Override
  protected boolean esConsensuado(Hecho hecho, List<Set<Hecho>> hechosDeFuentes) {
    long umbralDeMayoria = calcularUmbralDeMayoria(hechosDeFuentes.size());

    long menciones = hechosDeFuentes.stream()
                                    .filter(hechos -> hechos.contains(hecho))
                                    .count();

    return menciones >= umbralDeMayoria;
  }
}

//mayoría simple: si al menos la mitad de las fuentes del nodo contienen el mismo hecho,
// se lo considera consensuado;
