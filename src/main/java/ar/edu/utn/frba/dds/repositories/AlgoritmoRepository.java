package ar.edu.utn.frba.dds.repositories;

import ar.edu.utn.frba.dds.model.coleccion.algoritmosconsenso.AlgoritmoDeConsenso;
import ar.edu.utn.frba.dds.utils.DBUtils;
import javax.persistence.EntityManager;

public class AlgoritmoRepository {
  private final EntityManager em = DBUtils.getEntityManager();

  public void save(AlgoritmoDeConsenso algoritmo) {
    DBUtils.comenzarTransaccion(em);
    em.persist(algoritmo);
    DBUtils.commit(em);
  }
}
