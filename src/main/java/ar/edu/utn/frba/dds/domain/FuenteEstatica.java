package ar.edu.utn.frba.dds.domain;

//Para esta iteración, se requiere diseñar e implementar el componente que posibilite la lectura de estos datasets y
// que extraiga los hechos de los mismos. En esta primera iteración estaremos incorporando un lote de datos estático
// de tipo archivo .csv.

import java.util.List;

//Se tiene que leer un archivo .csv que contiene toda la info, no termino de entender si es una clase o como se maneja una fuente estatica.
public class FuenteEstatica extends Fuente {

  private String pathArchivoCSV;

  public FuenteEstatica(String nombre, String pathArchivoCSV) {
    super(nombre);
    this.pathArchivoCSV = pathArchivoCSV;
  }

  @Override
  public List<Hecho> obtenerHechos() {
    return List.of(); // TODO
  }
}
