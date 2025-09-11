package ar.edu.utn.frba.dds.domain.coleccion.algoritmosconsenso;

import ar.edu.utn.frba.dds.domain.fuentes.Fuente;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.List;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("Mult_menciones")
public class MultiplesMenciones extends AlgoritmoDeConsenso {
  @Override
  public List<Hecho> listaDeHechosConsensuados(
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
                       .filter(fuente -> fuente.obtenerHechos()
                                               .contains(hecho))
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