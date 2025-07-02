package ar.edu.utn.frba.dds.domain.AlgoritmosConcenso;

import ar.edu.utn.frba.dds.domain.fuentes.Fuente;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.List;

public interface AlgoritmoDeConcenso {
  public List<Hecho> listaDeHechosConcensuados(List<Hecho> listaDeHechos, List<Fuente> fuentesNodo);
}