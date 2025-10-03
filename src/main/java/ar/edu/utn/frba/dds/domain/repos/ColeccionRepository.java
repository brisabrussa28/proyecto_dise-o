package ar.edu.utn.frba.dds.domain.repos;

import ar.edu.utn.frba.dds.domain.coleccion.Coleccion;
import ar.edu.utn.frba.dds.domain.utils.DBUtils;
import java.util.List;
import javax.persistence.EntityManager;

public class ColeccionRepository {
  EntityManager em = DBUtils.getEntityManager();

  public ColeccionRepository() {
  }

  public void save(Coleccion coleccion) {
    DBUtils.comenzarTransaccion(em);
    em.persist(coleccion);
    DBUtils.commit(em);
  }

  public List<Coleccion> findAll() {
    return em.createQuery("SELECT * FROM coleccion", Coleccion.class)
             .getResultList();

  }

  public Coleccion findById(Long id) {
    return em.createQuery("SELECT * FROM coleccion WHERE coleccion_id = id", Coleccion.class)
             .setParameter("id", id)
             .getSingleResult();
  }
}