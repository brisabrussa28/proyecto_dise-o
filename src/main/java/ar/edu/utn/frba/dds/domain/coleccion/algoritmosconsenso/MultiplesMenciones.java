package ar.edu.utn.frba.dds.domain.coleccion.algoritmosconsenso;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.List;
import java.util.Set;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
/*
* MULTIPLES MENCIONES
* si al menos dos fuentes del nodo contienen un mismo hecho
* y ninguna otra fuente del nodo contiene otro de igual título
* pero diferentes atributos, se lo considera consensuado;
*/

@Entity
@DiscriminatorValue("Mult_menciones")
public class MultiplesMenciones extends AlgoritmoDeConsenso {
  @Transient
  private static final int MINIMO_MENCIONES = 2;

  /**
   * Verifica si un hecho cumple con todas las reglas para ser considerado consensuado.
   */
  @Override
  protected boolean esConsensuado(Hecho hecho, List<Set<Hecho>> hechosPorFuente) {
    return tieneMencionesSuficientes(hecho, hechosPorFuente)
        && !existeVersionConflictiva(hecho, hechosPorFuente);
  }

  /**
   * Regla 1: Verifica si el hecho es mencionado por al menos dos fuentes.
   */
  private boolean tieneMencionesSuficientes(Hecho hecho, List<Set<Hecho>> hechosPorFuente) {
    long conteo = hechosPorFuente.stream()
                                 .filter(setDeHechos -> setDeHechos.contains(hecho))
                                 .count();
    return conteo >= MINIMO_MENCIONES;
  }

  /**
   * Regla 2: Verifica si existe un hecho conflictivo en CUALQUIER fuente.
   */
  private boolean existeVersionConflictiva(Hecho hechoAComparar, List<Set<Hecho>> hechosPorFuente) {
    return hechosPorFuente.stream()
                          .flatMap(Set::stream) // Junta todos los hechos de todos los sets
                          .anyMatch(otroHecho -> esUnConflicto(hechoAComparar, otroHecho));
  }

  /**
   * Determina si dos hechos son conflictivos: mismo título, pero no son idénticos.
   */
  private boolean esUnConflicto(Hecho hechoOriginal, Hecho otroHecho) {
    return hechoOriginal.getHecho_titulo().equals(otroHecho.getHecho_titulo())
        && !hechoOriginal.equals(otroHecho);
  }
}