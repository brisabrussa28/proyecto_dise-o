package ar.edu.utn.frba.dds.domain.repos;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.utils.DBUtils;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.Optional;

public class HechoRepository {
  private final EntityManager em = DBUtils.getEntityManager();

  public HechoRepository() {
  }

  public void save(Hecho hecho) {
    if (hecho == null) return;
    DBUtils.comenzarTransaccion(em);
    DBUtils.enriquecerHecho(hecho); // Lógica de enriquecimiento centralizada
    em.persist(hecho);
    DBUtils.commit(em);
  }

  public List<Hecho> findAll() {
    // JPQL usa el nombre de la entidad, no de la tabla.
    return em.createQuery("SELECT h FROM Hecho h", Hecho.class)
             .getResultList();
  }

  public Optional<Hecho> findById(Long id) {
    try {
      Hecho hecho = em.createQuery("SELECT h FROM Hecho h WHERE h.id = :id", Hecho.class)
                      .setParameter("id", id)
                      .getSingleResult();
      return Optional.of(hecho);
    } catch (NoResultException e) {
      return Optional.empty();
    }
  }
}
