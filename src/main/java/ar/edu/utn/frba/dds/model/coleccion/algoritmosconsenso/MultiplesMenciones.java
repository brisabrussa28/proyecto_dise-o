package ar.edu.utn.frba.dds.model.coleccion.algoritmosconsenso;

import ar.edu.utn.frba.dds.model.hecho.Hecho;
import java.util.List;
import java.util.Set;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@DiscriminatorValue("Mult_menciones")
public class MultiplesMenciones extends AlgoritmoDeConsenso {
  @Transient
  private static final int MINIMO_MENCIONES = 2;

  @Override
  protected boolean esConsensuado(Hecho hecho, List<Set<Hecho>> hechosPorFuente) {
    return tieneMencionesSuficientes(hecho, hechosPorFuente)
        && !existeVersionConflictiva(hecho, hechosPorFuente);
  }

  private boolean tieneMencionesSuficientes(Hecho hecho, List<Set<Hecho>> hechosPorFuente) {
    long conteo = hechosPorFuente.stream()
                                 .filter(setDeHechos -> existeHechoSimilarEnFuente(setDeHechos, hecho))
                                 .count();
    return conteo >= MINIMO_MENCIONES;
  }

  private boolean existeVersionConflictiva(Hecho hechoAComparar, List<Set<Hecho>> hechosPorFuente) {
    return hechosPorFuente.stream()
                          .flatMap(Set::stream)
                          .anyMatch(otroHecho -> esUnConflicto(hechoAComparar, otroHecho));
  }

  private boolean esUnConflicto(Hecho hechoOriginal, Hecho otroHecho) {
    // Es conflicto si tienen mismo título (flexible) pero NO son el mismo hecho (equals estricto)
    boolean mismoTitulo = hechoOriginal.getTitulo() != null
        && hechoOriginal.getTitulo().trim().equalsIgnoreCase(otroHecho.getTitulo().trim());

    return mismoTitulo && !hechoOriginal.equals(otroHecho);
  }
}