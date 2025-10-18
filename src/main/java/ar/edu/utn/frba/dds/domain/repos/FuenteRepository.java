package ar.edu.utn.frba.dds.domain.repos;

import ar.edu.utn.frba.dds.model.fuentes.Fuente;
import ar.edu.utn.frba.dds.utils.DBUtils;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.Optional;

public class FuenteRepository {

  private final EntityManager em = DBUtils.getEntityManager();

  public FuenteRepository() {
  }

  public void save(Fuente fuente) {
    if (fuente == null) return;
    DBUtils.comenzarTransaccion(em);
    // Usamos el método centralizado para enriquecer cada hecho de la fuente.
    fuente.getHechos().forEach(DBUtils::enriquecerHecho);
    em.persist(fuente);
    DBUtils.commit(em);
  }

  public List<Fuente> findAll() {
    // La consulta debe devolver una lista de 'Fuente'.
    return em.createQuery("SELECT f FROM Fuente f", Fuente.class)
             .getResultList();
  }

  public Optional<Fuente> findById(Long id) {
    try {
      // La consulta debe ser sobre la entidad 'Fuente' y devolver una instancia de ella.
      Fuente fuente = em.createQuery("SELECT f FROM Fuente f WHERE f.id = :id", Fuente.class)
                        .setParameter("id", id)
                        .getSingleResult();
      return Optional.of(fuente);
    } catch (NoResultException e) {
      return Optional.empty();
    }
  }
}
