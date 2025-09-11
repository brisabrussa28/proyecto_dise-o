package ar.edu.utn.frba.dds.domain.coleccion.algoritmosconsenso;

import ar.edu.utn.frba.dds.domain.fuentes.Fuente;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.List;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("May_simple")
public class MayoriaSimple extends AlgoritmoDeConsenso {
  @Override
  public List<Hecho> listaDeHechosConsensuados(
      List<Hecho> listaDeHechos,
      List<Fuente> fuentesNodo
  ) {

    return listaDeHechos.stream()
                        .distinct()
                        .filter(hecho -> hechoEnMayoriaDeFuentes(hecho, fuentesNodo))
                        .toList();
  }

  boolean hechoEnMayoriaDeFuentes(Hecho hecho, List<Fuente> fuentes) {
    return ((int) Math.ceil((double) fuentes.size() / 2)) <= fuentes.stream()
                                                                    .distinct()
                                                                    .filter(fuente -> fuente.obtenerHechos()
                                                                                            .contains(hecho))
                                                                    .toList()
                                                                    .size();
  }
}

//mayoría simple: si al menos la mitad de las fuentes del nodo contienen el mismo hecho,
// se lo considera consensuado;
