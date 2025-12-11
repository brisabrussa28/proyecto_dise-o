package ar.edu.utn.frba.dds.model.coleccion.algoritmosconsenso;

import ar.edu.utn.frba.dds.model.fuentes.Fuente;
import ar.edu.utn.frba.dds.model.fuentes.FuenteDeAgregacion;
import ar.edu.utn.frba.dds.model.hecho.Estado;
import ar.edu.utn.frba.dds.model.hecho.Hecho;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "algoritmo_tipo")
@Table(name = "AlgoritmoDeConsenso")
public abstract class AlgoritmoDeConsenso {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long algoritmo_id;

  public List<Hecho> listaDeHechosConsensuados(
      List<Hecho> listaDeHechos,
      List<Fuente> fuentes
  ) {

    List<Set<Hecho>> hechosPorFuenteEnSets = obtenerListasDeHechos(fuentes);

    return listaDeHechos.stream()
                        .distinct()
                        .filter(hecho -> esConsensuado(hecho, hechosPorFuenteEnSets))
                        .toList();
  }

  /**
   * Método auxiliar para obtener la lista de fuentes subyacentes.
   */
  List<Fuente> obtenerFuentesDelNodo(Fuente fuente) {
    if (fuente instanceof FuenteDeAgregacion agregador) {
      return agregador.getFuentesCargadas();
    } else {
      return List.of(fuente);
    }
  }

  /**
   * Obtiene la lista de hechos de cada fuente, excluyendo los eliminados.
   */
  List<Set<Hecho>> obtenerListasDeHechos(List<Fuente> fuentes) {
    return fuentes.stream()
                  .map(fuente ->
                           fuente.getHechos()
                                 .stream()
                                 .filter(hecho -> hecho.getEstado() != Estado.ELIMINADO)
                                 .collect(Collectors.toSet())
                  )
                  .toList();
  }

  protected abstract boolean esConsensuado(Hecho hecho, List<Set<Hecho>> hechosDeFuentes);
}