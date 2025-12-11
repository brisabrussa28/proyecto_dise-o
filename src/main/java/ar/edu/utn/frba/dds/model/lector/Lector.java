package ar.edu.utn.frba.dds.model.lector;

import java.io.InputStream;
import java.util.List;

public interface Lector<T> {
  List<T> importar(String path);

  List<T> importar(InputStream contenido);
}
