package ar.edu.utn.frba.dds.model.exportador;

import java.util.List;

public interface Exportador<T> {
  void exportar(List<T> objetos, String path);

}
