package ar.edu.utn.frba.dds.domain.repos;

import ar.edu.utn.frba.dds.domain.hecho.etiqueta.Etiqueta;
import ar.edu.utn.frba.dds.domain.utils.DBUtils;
import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

public class EtiquetaRepository {
  private final EntityManager em = DBUtils.getEntityManager();

  public EtiquetaRepository() {
  }

  public void save(Etiqueta etiqueta) {
    if (etiqueta == null) return;
    DBUtils.comenzarTransaccion(em);
    em.persist(etiqueta);
    DBUtils.commit(em);
  }

  public List<Etiqueta> findAll() {
    // La consulta debe devolver todas las entidades 'Etiqueta'.
    return em.createQuery("SELECT e FROM Etiqueta e", Etiqueta.class)
             .getResultList();
  }

  public Optional<Etiqueta> findById(Long id) {
    try {
      // La consulta busca una 'Etiqueta' por su ID.
      Etiqueta etiqueta = em.createQuery("SELECT e FROM Etiqueta e WHERE e.id = :id", Etiqueta.class)
                            .setParameter("id", id)
                            .getSingleResult();
      return Optional.of(etiqueta);
    } catch (NoResultException e) {
      return Optional.empty();
    }
  }
}
