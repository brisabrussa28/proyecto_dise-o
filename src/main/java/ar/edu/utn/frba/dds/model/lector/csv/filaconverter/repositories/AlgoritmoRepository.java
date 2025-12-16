package ar.edu.utn.frba.dds.model.lector.csv.filaconverter.repositories;

import ar.edu.utn.frba.dds.model.coleccion.algoritmosconsenso.AlgoritmoDeConsenso;
import ar.edu.utn.frba.dds.utils.DBUtils;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

public class AlgoritmoRepository {

  private static final AlgoritmoRepository INSTANCE = new AlgoritmoRepository();

  public static AlgoritmoRepository instance() {
    return INSTANCE;
  }

  public void save(AlgoritmoDeConsenso algoritmo) {
    EntityManager em = DBUtils.getEntityManager();
    try {
      DBUtils.comenzarTransaccion(em);
      em.persist(algoritmo);
      DBUtils.commit(em);
    } catch (PersistenceException e) {
      DBUtils.rollback(em);
      throw new RuntimeException(e.getMessage());
    } finally {
      em.close();
    }
  }

  public AlgoritmoDeConsenso findById(Long id) {
    EntityManager em = DBUtils.getEntityManager();
    try {
      return em.find(AlgoritmoDeConsenso.class, id);
    } finally {
      em.close();
    }
  }
}