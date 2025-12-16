package ar.edu.utn.frba.dds.model.lector.csv.filaconverter.repositories;

import ar.edu.utn.frba.dds.model.hecho.Hecho;
import ar.edu.utn.frba.dds.model.hecho.etiqueta.Etiqueta;
import ar.edu.utn.frba.dds.utils.DBUtils;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

public class EtiquetaRepository {

  public void save(Etiqueta etiqueta) {
    EntityManager em = DBUtils.getEntityManager();
    try {
      DBUtils.comenzarTransaccion(em);
      em.persist(etiqueta);
      DBUtils.commit(em); // Commit dentro del try
    } catch (PersistenceException e) {
      DBUtils.rollback(em);
      throw new RuntimeException(e.getMessage());
    } finally {
      em.close();
    }
  }

  public List<Hecho> findAll() {
    EntityManager em = DBUtils.getEntityManager();
    try {
      return em.createQuery("SELECT h FROM Hecho h", Hecho.class)
               .getResultList();
    } finally {
      em.close();
    }
  }
}