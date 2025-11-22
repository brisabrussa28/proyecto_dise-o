package ar.edu.utn.frba.dds.model.coleccion.algoritmosconsenso;

import ar.edu.utn.frba.dds.model.hecho.Hecho;
import java.util.List;
import java.util.Set;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
/*
* ABSOLUTA
* si todas las fuentes del nodo contienen el mismo, se lo considera consensuado.
*/



@Entity
@DiscriminatorValue("Absoluta")
public class Absoluta extends AlgoritmoDeConsenso {

  /**
   * Verifica si un hecho cumple con el consenso absoluto.
   */
  @Override
  protected boolean esConsensuado(Hecho hecho, List<Set<Hecho>> hechosDeFuentes) {
    return hechosDeFuentes.stream()
                          .allMatch(hechos -> hechos.contains(hecho));
  }
}


