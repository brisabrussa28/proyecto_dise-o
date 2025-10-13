package ar.edu.utn.frba.dds.repositories;

import ar.edu.utn.frba.dds.model.hecho.Hecho;
import ar.edu.utn.frba.dds.utils.DBUtils;
import java.util.List;
import javax.persistence.EntityManager;

public class HechoRepository {
  EntityManager em = DBUtils.getEntityManager();

  public HechoRepository() {
  }

  public void save(Hecho hecho) {
    DBUtils.comenzarTransaccion(em);
    DBUtils.completarUbicacionFaltante(hecho);
    DBUtils.completarProvinciaFaltante(hecho);
    em.persist(hecho);
    DBUtils.commit(em);
  }

  public List<Hecho> findAll() {
    return em.createQuery("SELECT * FROM hecho", Hecho.class)
             .getResultList();

  }

  public Hecho getById(Long id) {
    return em.createQuery("SELECT * FROM hecho WHERE hecho_id = id", Hecho.class)
             .setParameter("id", id)
             .getSingleResult();
  }
}
