package ar.edu.utn.frba.dds.repositories;

import ar.edu.utn.frba.dds.model.coleccion.Coleccion;
import ar.edu.utn.frba.dds.utils.DBUtils;
import java.util.List;
import javax.persistence.EntityManager;

public class ColeccionRepository {
  private final EntityManager em = DBUtils.getEntityManager();

  private static final ColeccionRepository INSTANCE = new ColeccionRepository();

  public static ColeccionRepository instance() {
    return INSTANCE;
  }

  // java
  public void save(Coleccion coleccion) {
    DBUtils.comenzarTransaccion(em);

    coleccion.getHechosConsensuados()
             .forEach(DBUtils::enriquecerHecho);

    if (coleccion.getId() == null || findById(coleccion.getId()) == null) {
      em.persist(coleccion);
    } else {
      em.merge(coleccion);
    }

    DBUtils.commit(em);
  }

  public List<Coleccion> findAll() {
    return em.createQuery("select c from Coleccion c", Coleccion.class)
             .getResultList();

  }

  public Coleccion findById(Long id) {
    return em.find(Coleccion.class, id);
  }

  public List<String> getCategorias() {
    return em.createQuery("select distinct c.coleccion_categoria from Coleccion c", String.class)
             .getResultList();
  }

  public Long countAll() {
    return em.createQuery("SELECT COUNT(DISTINCT c.id) FROM Coleccion c", Long.class)
             .getSingleResult();
  }

}