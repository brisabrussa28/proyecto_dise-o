package ar.edu.utn.frba.dds.repositories;

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
    DBUtils.comenzarTransaccion(em);
    try {
      em.persist(algoritmo);
    } catch (PersistenceException e) {
      DBUtils.rollback(em);
      throw new RuntimeException(e.getMessage());
    } finally {
      DBUtils.commit(em);
    }
  }

  public AlgoritmoDeConsenso findById(Long id) {
    EntityManager em = DBUtils.getEntityManager();
    return em.find(AlgoritmoDeConsenso.class, id);
  }
}
