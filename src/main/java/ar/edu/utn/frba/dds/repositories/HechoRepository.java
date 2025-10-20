package ar.edu.utn.frba.dds.repositories;

import ar.edu.utn.frba.dds.model.hecho.Hecho;
import ar.edu.utn.frba.dds.utils.DBUtils;
import java.util.List;
import javax.persistence.EntityManager;

public class HechoRepository {
  private final EntityManager em = DBUtils.getEntityManager();

  public void save(Hecho hecho) {
    DBUtils.comenzarTransaccion(em);
    if (hecho.getId() == null) {
      em.persist(hecho);
    } else {
      em.merge(hecho);
    }
    DBUtils.commit(em);
  }

  public List<Hecho> findAll() {
    return em.createQuery("SELECT h FROM Hecho h", Hecho.class)
                          .getResultList();
  }

  public Hecho getById(Long id) {
    return em.find(Hecho.class, id);
  }
}