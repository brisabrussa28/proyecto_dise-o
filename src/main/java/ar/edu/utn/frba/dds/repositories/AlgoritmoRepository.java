package ar.edu.utn.frba.dds.repositories;

import ar.edu.utn.frba.dds.model.coleccion.algoritmosconsenso.AlgoritmoDeConsenso;
import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;

public class AlgoritmoRepository implements WithSimplePersistenceUnit {
  private static final AlgoritmoRepository INSTANCE = new AlgoritmoRepository();

  public static AlgoritmoRepository instance() {
    return INSTANCE;
  }

  public void save(AlgoritmoDeConsenso algoritmo) {
    entityManager().persist(algoritmo);
  }
}
