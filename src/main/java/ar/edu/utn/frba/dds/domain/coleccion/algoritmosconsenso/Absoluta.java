package ar.edu.utn.frba.dds.domain.coleccion.algoritmosconsenso;

import ar.edu.utn.frba.dds.domain.fuentes.Fuente;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.List;
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

    return listaDeHechos.stream()
                        .distinct()
                        .filter(hecho -> hechoEnTodasLasFuentes(hecho, fuentesNodo))
                        .toList();
  }

  boolean hechoEnTodasLasFuentes(Hecho hecho, List<Fuente> fuentes) {
    return fuentes.stream()
                  .allMatch(fuente -> fuente.obtenerHechos()
                                            .contains(hecho));
  }
}

//absoluta: si todas las fuentes del nodo contienen el mismo, se lo considera consensuado.

