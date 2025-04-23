package ar.edu.utn.frba.dds.domain;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class ServicioDeAgregacion extends Fuente {
  private List<Fuente> fuentesCargadas;
  private List<Hecho> hechosAcumulados;
  //Podriamos hacer que los guarde en un archivo en disco? no creo que tener una lista de hechos cargada en memoria sea bueno...

  public ServicioDeAgregacion(String nombre) {
    super(nombre);
    this.fuentesCargadas = new ArrayList<>();
    this.hechosAcumulados = new ArrayList<>();
  }

  public void agregarFuente(Fuente fuente) {
    // Agregar la fuente a la lista de fuentes cargadas
    fuentesCargadas.add(fuente);
    // Acumular los hechos de la fuente en la lista de hechos
    this.agregarHechos(fuente.obtenerHechos());
  }

  private void agregarHechos(List<Hecho> nuevosHechos) {
    hechosAcumulados.addAll(nuevosHechos);
  }

  @Override
  public List<Hecho> obtenerHechos() {
    // Devuelve los hechos acumulados
    return hechosAcumulados;
  }
}
