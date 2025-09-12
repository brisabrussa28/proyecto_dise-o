package ar.edu.utn.frba.dds.domain.serializadores.Lector;

import java.util.List;

public interface Lector <T> {
  List<T> importar(String path);
  String getConfiguracionJson();
}
