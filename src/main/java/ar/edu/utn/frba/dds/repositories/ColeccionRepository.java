package ar.edu.utn.frba.dds.repositories;

import ar.edu.utn.frba.dds.model.coleccion.Coleccion;
import ar.edu.utn.frba.dds.utils.DBUtils;
import java.util.List;
import javax.persistence.EntityManager;

public class ColeccionRepository {
  private final EntityManager em = DBUtils.getEntityManager();

  public void save(Coleccion coleccion) {
    DBUtils.comenzarTransaccion(em);
    coleccion.getHechosConsensuados()
             .forEach(hecho -> {
               DBUtils.enriquecerHecho(hecho);
             });
    em.persist(coleccion);
    DBUtils.commit(em);
  }

  public List<Coleccion> findAll() {
    return em.createQuery("select c from Coleccion c", Coleccion.class)
             .getResultList();

  }

  public Coleccion findById(Long id) {
    return em.find(Coleccion.class, id);
  }
}