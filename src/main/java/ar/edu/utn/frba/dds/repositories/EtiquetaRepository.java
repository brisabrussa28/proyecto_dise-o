package ar.edu.utn.frba.dds.repositories;

import ar.edu.utn.frba.dds.model.hecho.Hecho;
import ar.edu.utn.frba.dds.model.hecho.etiqueta.Etiqueta;
import ar.edu.utn.frba.dds.utils.DBUtils;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

public class EtiquetaRepository {

  public void save(Etiqueta etiqueta) {
    EntityManager em = DBUtils.getEntityManager();
    DBUtils.comenzarTransaccion(em);
    try {
      em.persist(etiqueta);
    } catch (PersistenceException e) {
      DBUtils.rollback(em);
      throw new RuntimeException(e.getMessage());
    } finally {
      DBUtils.commit(em);
      em.close();
    }
  }

  public List<Hecho> findAll() {
    EntityManager em = DBUtils.getEntityManager();
    return em.createQuery("SELECT h FROM Hecho h", Hecho.class)
             .getResultList();

  }
}
