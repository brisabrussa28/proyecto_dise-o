package ar.edu.utn.frba.dds.domain.repos;

import ar.edu.utn.frba.dds.model.estadisticas.Estadistica;
import ar.edu.utn.frba.dds.utils.DBUtils;
import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

public class EstadisticaRepository {
  private final EntityManager em = DBUtils.getEntityManager();

  public EstadisticaRepository() {
  }

  public void save(Estadistica estadistica) {
    if (estadistica == null) return;
    DBUtils.comenzarTransaccion(em);
    em.persist(estadistica);
    DBUtils.commit(em);
  }

  public List<Estadistica> findAll() {
    // JPQL usa el nombre de la entidad.
    return em.createQuery("SELECT e FROM Estadistica e", Estadistica.class)
             .getResultList();
  }

  public Optional<Estadistica> findById(Long id) {
    try {
      // JPQL usa el nombre de la entidad y sus campos.
      Estadistica estadistica = em.createQuery("SELECT e FROM Estadistica e WHERE e.id = :id", Estadistica.class)
                                  .setParameter("id", id)
                                  .getSingleResult();
      return Optional.of(estadistica);
    } catch (NoResultException e) {
      return Optional.empty();
    }
  }
}
