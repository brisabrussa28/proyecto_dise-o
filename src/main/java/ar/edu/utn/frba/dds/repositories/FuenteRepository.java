package ar.edu.utn.frba.dds.repositories;

import ar.edu.utn.frba.dds.model.fuentes.Fuente;
import ar.edu.utn.frba.dds.utils.DBUtils;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

public class FuenteRepository {
  private static final FuenteRepository INSTANCE = new FuenteRepository();

  public static FuenteRepository instance() {
    return INSTANCE;
  }

  public void save(Fuente fuente) {
    EntityManager em = DBUtils.getEntityManager();
    fuente.getHechos()
          .forEach(DBUtils::enriquecerHecho);
    DBUtils.comenzarTransaccion(em);
    try {
      em.persist(fuente);
    } catch (PersistenceException e) {
      DBUtils.rollback(em);
      throw new RuntimeException(e.getMessage());
    } finally {
      DBUtils.commit(em);
      em.close();
    }
  }

  public List<Fuente> findAll() {
    EntityManager em = DBUtils.getEntityManager();
    return em.createQuery("SELECT f FROM Fuente f", Fuente.class)
             .getResultList();

  }

  public Fuente findById(Long id) {
    EntityManager em = DBUtils.getEntityManager();
    return em.find(Fuente.class, id);
  }

}
