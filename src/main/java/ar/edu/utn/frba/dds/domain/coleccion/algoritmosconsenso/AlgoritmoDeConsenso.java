package ar.edu.utn.frba.dds.domain.coleccion.algoritmosconsenso;

import ar.edu.utn.frba.dds.domain.filtro.Filtro;
import ar.edu.utn.frba.dds.domain.fuentes.Fuente;
import ar.edu.utn.frba.dds.domain.fuentes.FuenteDeAgregacion;
import ar.edu.utn.frba.dds.domain.hecho.Estado;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Transient;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_algoritmo")
public abstract class AlgoritmoDeConsenso {
  @Id
  @GeneratedValue
  Long algoritmo_id;

  @Transient
  private List<Hecho> hechosConsensuados = new ArrayList<>();

  // TODO: Revisar como se comporta el sistema si no hay fuentes

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
   * Obtiene la lista de hechos de cada fuente, excluyendo los eliminados,
   * y la convierte a un Set.
   */
  List<Set<Hecho>> obtenerListasDeHechos(List<Fuente> fuentes) {
    return fuentes.stream()
                  .map(fuente ->
                           fuente.obtenerHechos().stream()
                                 .filter(hecho -> hecho.getEstado() != Estado.ELIMINADO) //filtra eliminados
                                 .collect(Collectors.toSet())
                  )
                  .toList();
  }

  /**
   * Recalcula y actualiza la lista interna de hechos consensuados.
   *
   * @param hechosColeccion hechos de la coleccion que aplica al algoriitmo
   * @param fuentes Lista de fuentes sobre la cual se aplica el algoritmo
   */
  public void recalcularHechosConsensuados(List<Hecho> hechosColeccion, List<Fuente> fuentes) {
    this.hechosConsensuados = this.listaDeHechosConsensuados(hechosColeccion, fuentes);
  }

  public List<Hecho> getHechosConsensuados() {
    return Collections.unmodifiableList(hechosConsensuados);
  }



  protected abstract boolean esConsensuado(Hecho hecho, List<Set<Hecho>> hechosDeFuentes);
}