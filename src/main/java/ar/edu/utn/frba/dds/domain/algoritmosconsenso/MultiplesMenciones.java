package ar.edu.utn.frba.dds.domain.algoritmosconsenso;

import ar.edu.utn.frba.dds.domain.fuentes.Fuente;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.List;

public class MultiplesMenciones implements AlgoritmoDeConsenso {
  @Override
  public List<Hecho> listaDeHechosConsensuados(
      List<Hecho> listaDeHechos,
      List<Fuente> fuentesNodo
  ) {

    return listaDeHechos.stream().distinct()
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
        .flatMap(f -> f.obtenerHechos().stream())
        .anyMatch(h -> h.getTitulo().equals(hecho.getTitulo()) && !h.equals(hecho));
  }
}