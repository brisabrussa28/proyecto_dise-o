package ar.edu.utn.frba.dds.domain.serializadores.lector;

import java.util.List;

public interface Lector<T> {
  List<T> importar(String path);

  String getConfiguracionJson();
}
