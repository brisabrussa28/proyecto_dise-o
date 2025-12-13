package ar.edu.utn.frba.dds.repositories;

import ar.edu.utn.frba.dds.model.coleccion.Coleccion;
import ar.edu.utn.frba.dds.utils.DBUtils;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

public class ColeccionRepository {
  private static final ColeccionRepository INSTANCE = new ColeccionRepository();

  public static ColeccionRepository instance() {
    return INSTANCE;
  }

  // java
  public void save(Coleccion coleccion) {
    EntityManager em = DBUtils.getEntityManager();
    coleccion.getHechosConsensuados()
             .forEach(DBUtils::enriquecerHecho);
    DBUtils.comenzarTransaccion(em);
    try {
      if (coleccion.getId() == null || findById(coleccion.getId()) == null) {
        em.persist(coleccion);
      } else {
        em.merge(coleccion);
      }

    } catch (PersistenceException e) {
      DBUtils.rollback(em);
      throw new RuntimeException(e.getMessage());
    } finally {
      DBUtils.commit(em);
      em.close();
    }
  }

  public List<Coleccion> findAll() {
    EntityManager em = DBUtils.getEntityManager();
    return em.createQuery("select c from Coleccion c", Coleccion.class)
             .getResultList();

  }

  public Coleccion findById(Long id) {
    EntityManager em = DBUtils.getEntityManager();
    return em.find(Coleccion.class, id);
  }

  public List<String> getCategorias() {
    EntityManager em = DBUtils.getEntityManager();
    return em.createQuery("select distinct c.coleccion_categoria from Coleccion c", String.class)
             .getResultList();
  }

  public Long countAll() {
    EntityManager em = DBUtils.getEntityManager();
    return em.createQuery("SELECT COUNT(DISTINCT c.id) FROM Coleccion c", Long.class)
             .getSingleResult();
  }

}