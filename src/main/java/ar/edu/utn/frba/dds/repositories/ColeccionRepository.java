package ar.edu.utn.frba.dds.repositories;

import ar.edu.utn.frba.dds.model.coleccion.Coleccion;
import ar.edu.utn.frba.dds.utils.DBUtils;
import java.util.List;
import javax.persistence.EntityManager;

public class ColeccionRepository {
  EntityManager em = DBUtils.getEntityManager();

  public ColeccionRepository() {
  }

  public void save(Coleccion coleccion) {
    DBUtils.comenzarTransaccion(em);
    coleccion.getHechosConsensuados()
             .forEach(hecho -> {
               DBUtils.completarProvinciaFaltante(hecho);
               DBUtils.completarUbicacionFaltante(hecho);
             });
    em.persist(coleccion);
    DBUtils.commit(em);
  }

  public List<Coleccion> findAll() {
    return em.createQuery("SELECT c FROM Coleccion c", Coleccion.class)
             .getResultList();

  }

  public Coleccion findById(Long id) {
    return em.createQuery("SELECT c FROM Coleccion c WHERE c.coleccion_id = :id", Coleccion.class)
             .setParameter("id", id)
             .getSingleResult();
  }
}