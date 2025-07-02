package ar.edu.utn.frba.dds.domain.AlgoritmosConcenso;

import ar.edu.utn.frba.dds.domain.fuentes.Fuente;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.List;

public class MultiplesMenciones implements AlgoritmoDeConcenso {
  @Override
  public List<Hecho> listaDeHechosConcensuados(
      List<Hecho> listaDeHechos,
      List<Fuente> fuentesNodo
  ) {

    return listaDeHechos.stream()
        .filter(hecho -> !hechoConVersionDistinta(
            hecho,
            fuentesNodo
        ) && hechoEnDosOMasFuentes(hecho, fuentesNodo))
        .toList();
  }
  
  boolean hechoEnDosOMasFuentes(Hecho hecho, List<Fuente> fuentes) {
    return 2 <= fuentes.stream()
        .filter(fuente -> fuente.obtenerHechos().contains(hecho))
        .toList()
        .size();
  }

  boolean hechoConVersionDistinta(Hecho hecho, List<Fuente> fuentes) {
    return fuentes.stream()
        .map(Fuente::obtenerHechos)
        .filter(hechos -> hechos.stream()
            .anyMatch(hecho1 -> hecho1.getTitulo()
                .equals(hecho.getTitulo())))

        .anyMatch(hechos -> hechos.stream()
            .anyMatch(hecho1 -> !hecho1.getTitulo()
                .equals(hecho.getTitulo())));
  }

}