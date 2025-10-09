package ar.edu.utn.frba.dds.domain.coleccion.algoritmosconsenso;

import ar.edu.utn.frba.dds.domain.fuentes.Fuente;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDeAgregacion;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.List;
import java.util.Set;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_algoritmo")
public abstract class AlgoritmoDeConsenso {
  @Id
  @GeneratedValue
  Long algoritmo_id;

  public List<Hecho> listaDeHechosConsensuados(
      List<Hecho> listaDeHechos,
      Fuente fuente
  ) {
    List<Fuente> fuentesDelNodo = this.obtenerFuentesDelNodo(fuente);

    List<Set<Hecho>> hechosPorFuenteEnSets = obtenerListasDeHechos(fuentesDelNodo);

    return listaDeHechos.stream()
                        .distinct()
                        .filter(hecho -> esConsensuado(hecho, hechosPorFuenteEnSets))
                        .toList();
  }
  /**
   * Mét0do auxiliar para obtener la lista de fuentes subyacentes.
   * Si la fuente principal es un agregador, devuelve las fuentes que lo componen.
   * Si es una fuente simple, la devuelve en una lista unitaria.
   *
   * @return La lista de fuentes base.
   */
  List<Fuente> obtenerFuentesDelNodo(Fuente fuente) {
    if (fuente instanceof FuenteDeAgregacion agregador) {
      return agregador.getFuentesCargadas();
    } else {
      return List.of(fuente);
    }
  }

  /**
   * Obtiene la lista de hechos de cada fuente y la convierte a un Set.
   */
  List<Set<Hecho>> obtenerListasDeHechos(List<Fuente> fuentes) {
    return fuentes.stream()
                  .map(fuente -> Set.copyOf(fuente.getHechos()))
                  .toList();
  }

  protected abstract boolean esConsensuado(Hecho hecho, List<Set<Hecho>> hechosDeFuentes);
}