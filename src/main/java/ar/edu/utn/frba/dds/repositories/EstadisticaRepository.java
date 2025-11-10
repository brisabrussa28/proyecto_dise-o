package ar.edu.utn.frba.dds.repositories;

import ar.edu.utn.frba.dds.model.estadisticas.Estadistica;
import ar.edu.utn.frba.dds.utils.DBUtils;
import java.util.List;
import javax.persistence.EntityManager;

public class EstadisticaRepository {
  private final EntityManager em = DBUtils.getEntityManager();

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
    return em.createQuery(
                 "SELECT e FROM Estadistica e WHERE e.estadistica_nombre = :nombre AND e.estadistica_tipo = :tipo", Estadistica.class)
             .setParameter("nombre", nombre)
             .setParameter("tipo", tipoEstadistica)
             .getResultStream()
             .findFirst()
             .orElse(null);
  }

  // Update save method
  public void save(Estadistica estadistica) {
    Estadistica existing = findByNombreAndTipo(estadistica.getNombre(), estadistica.getTipo());
    DBUtils.comenzarTransaccion(em);
    if (existing != null) {
      estadistica.setId(existing.getId());
      em.merge(estadistica);
    } else {
      em.persist(estadistica);
    }
    DBUtils.commit(em);
  }


  public List<Estadistica> findAll() {
    return em.createQuery("SELECT f FROM Estadistica f", Estadistica.class)
             .getResultList();

  }

  public Estadistica findById(Long id) {
    return em.find(Estadistica.class, id);
  }
}
