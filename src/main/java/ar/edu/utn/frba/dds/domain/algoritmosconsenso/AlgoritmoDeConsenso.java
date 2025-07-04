package ar.edu.utn.frba.dds.domain.algoritmosconsenso;

import ar.edu.utn.frba.dds.domain.fuentes.Fuente;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.List;

public interface AlgoritmoDeConsenso {
  public List<Hecho> listaDeHechosConsensuados(List<Hecho> listaDeHechos, List<Fuente> fuentesNodo);
}