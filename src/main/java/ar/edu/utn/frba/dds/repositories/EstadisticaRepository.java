package ar.edu.utn.frba.dds.repositories;

import ar.edu.utn.frba.dds.model.estadisticas.Estadistica;
import ar.edu.utn.frba.dds.utils.DBUtils;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

public class EstadisticaRepository {

  private static final EstadisticaRepository INSTANCE = new EstadisticaRepository();

  public static EstadisticaRepository instance() {
    return INSTANCE;
  }

//  public void save(Estadistica estadistica) {
//    if (estadistica.getId() == null) {
//      DBUtils.comenzarTransaccion(em);
//      em.persist(estadistica);
//      DBUtils.commit(em);
//    } else {
//      DBUtils.comenzarTransaccion(em);
//      em.merge(estadistica);
//      DBUtils.commit(em);
//    }
//  }

  public Estadistica findByNombreAndTipo(String nombre, String tipoEstadistica) {
    EntityManager em = DBUtils.getEntityManager();
    return em.createQuery(
                 "SELECT e FROM Estadistica e WHERE e.estadistica_nombre = :nombre AND e.estadistica_tipo = :tipo",
                 Estadistica.class
             )
             .setParameter("nombre", nombre)
             .setParameter("tipo", tipoEstadistica)
             .getResultStream()
             .findFirst()
             .orElse(null);
  }

  // Update save method
  public void save(Estadistica estadistica) {
    EntityManager em = DBUtils.getEntityManager();
    Estadistica existing = findByNombreAndTipo(estadistica.getNombre(), estadistica.getTipo());
    DBUtils.comenzarTransaccion(em);
    try {
      if (existing != null) {
        estadistica.setId(existing.getId());
        em.merge(estadistica);
      } else {
        em.persist(estadistica);
      }
    } catch (PersistenceException e) {
      DBUtils.rollback(em);
      throw new RuntimeException(e.getMessage());
    } finally {
      DBUtils.commit(em);
      em.close();
    }
  }


  public List<Estadistica> findAll() {
    EntityManager em = DBUtils.getEntityManager();
    return em.createQuery("SELECT e FROM Estadistica e", Estadistica.class)
             .getResultList();

  }

  public Estadistica findById(Long id) {
    EntityManager em = DBUtils.getEntityManager();
    return em.find(Estadistica.class, id);
  }

  public Estadistica findByTipo(String tipo) {
    EntityManager em = DBUtils.getEntityManager();
    return em.createQuery(
                 "SELECT e from Estadistica e where e.estadistica_tipo = :tipo",
                 Estadistica.class
             )
             .setParameter("tipo", tipo)
             .getSingleResult();
  }

}
