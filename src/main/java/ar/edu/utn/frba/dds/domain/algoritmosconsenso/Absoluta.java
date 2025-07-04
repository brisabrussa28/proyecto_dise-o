package ar.edu.utn.frba.dds.domain.algoritmosconsenso;

import ar.edu.utn.frba.dds.domain.fuentes.Fuente;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.List;

public class Absoluta implements AlgoritmoDeConsenso {
  @Override
  public List<Hecho> listaDeHechosConsensuados(
      List<Hecho> listaDeHechos,
      List<Fuente> fuentesNodo
  ) {
    return listaDeHechos.stream()
        .filter(hecho -> hechoEnTodasLasFuentes(hecho, fuentesNodo)).toList();
  }

  boolean hechoEnTodasLasFuentes(Hecho hecho, List<Fuente> fuentes) {
    return fuentes.stream().allMatch(fuente -> fuente.obtenerHechos().contains(hecho));
  }
}

//absoluta: si todas las fuentes del nodo contienen el mismo, se lo considera consensuado.

