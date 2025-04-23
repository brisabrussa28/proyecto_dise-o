package ar.edu.utn.frba.dds.domain;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class ServicioDeAgregacion extends Fuente {
  private List<Fuente> fuentesCargadas;
  List<Hecho> hechos;

  public ServicioDeAgregacion(String nombre, List<Hecho> hechos) {
    super(nombre, hechos);
    this.fuentesCargadas = new ArrayList<>();
  }

  public void agregarFuente(Fuente fuente) {
    fuentesCargadas.add(fuente);
  }

  @Override
  public List<Hecho> obtenerHechos() {
    return fuentesCargadas.stream()
        .flatMap(fuente -> fuente.obtenerHechos().stream())
        .collect(Collectors.toList());
  }
}
