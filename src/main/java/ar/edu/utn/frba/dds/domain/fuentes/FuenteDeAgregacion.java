package ar.edu.utn.frba.dds.domain.fuentes;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FuenteDeAgregacion extends FuenteCacheable {

  private final List<Fuente> fuentesCargadas;

  public FuenteDeAgregacion(String nombre, String jsonFilePathParaCopias) {
    super(nombre, jsonFilePathParaCopias);
    this.fuentesCargadas = new ArrayList<>();
  }

  public void agregarFuente(Fuente fuente) {
    this.fuentesCargadas.add(fuente);
  }

  public List<Fuente> getFuentesCargadas() {
    return new ArrayList<>(this.fuentesCargadas);
  }

  @Override
  protected List<Hecho> consultarNuevosHechos() {
    return this.fuentesCargadas.stream()
                               .flatMap(fuente -> fuente.obtenerHechos()
                                                        .stream())
                               .distinct()
                               .collect(Collectors.toList());
  }
}
