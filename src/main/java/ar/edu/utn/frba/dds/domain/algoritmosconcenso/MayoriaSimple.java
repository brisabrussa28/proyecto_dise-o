package ar.edu.utn.frba.dds.domain.algoritmosconcenso;

import ar.edu.utn.frba.dds.domain.fuentes.Fuente;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.List;

public class MayoriaSimple implements AlgoritmoDeConcenso {

  @Override
  public List<Hecho> listaDeHechosConcensuados(
      List<Hecho> listaDeHechos,
      List<Fuente> fuentesNodo
  ) {

    return listaDeHechos.stream()
        .filter(hecho -> hechoEnMayoriaDeFuentes(hecho, fuentesNodo)).toList();
  }

  boolean hechoEnMayoriaDeFuentes(Hecho hecho, List<Fuente> fuentes) {
    return ((int) Math.ceil((double) fuentes.size() / 2)) <= fuentes.stream()
        .filter(fuente -> fuente.obtenerHechos().contains(hecho)).toList().size();
  }
}

//mayoría simple: si al menos la mitad de las fuentes del nodo contienen el mismo hecho,
// se lo considera consensuado;
