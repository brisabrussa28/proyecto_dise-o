package ar.edu.utn.frba.dds.model.lector;

import java.util.List;

public interface Lector<T> {
  List<T> importar(String path);

}
