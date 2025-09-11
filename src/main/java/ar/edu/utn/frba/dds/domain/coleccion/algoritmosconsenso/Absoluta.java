package ar.edu.utn.frba.dds.domain.coleccion.algoritmosconsenso;

import ar.edu.utn.frba.dds.domain.fuentes.Fuente;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("Absoluta")
public class Absoluta extends AlgoritmoDeConsenso {
  @Override
  public List<Hecho> listaDeHechosConsensuados(
      List<Hecho> listaDeHechos,
      List<Fuente> fuentesNodo
  ) {
    if (fuentesNodo.isEmpty()) {
      return List.of();
    }

    Set<Hecho> consensuados = new HashSet<>(fuentesNodo.get(0)
                                                       .obtenerHechos());
    for (Fuente fuente : fuentesNodo) {
      consensuados.retainAll(fuente.obtenerHechos());
    }

    return consensuados.stream()
                       .filter(listaDeHechos::contains)
                       .toList();
  }

  boolean hechoEnTodasLasFuentes(Hecho hecho, List<Fuente> fuentes) {
    return fuentes.stream()
                  .allMatch(fuente -> fuente.obtenerHechos()
                                            .contains(hecho));
  }
}

//absoluta: si todas las fuentes del nodo contienen el mismo, se lo considera consensuado.

