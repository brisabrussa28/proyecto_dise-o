package ar.edu.utn.frba.dds.domain.repos;

import ar.edu.utn.frba.dds.domain.coleccion.Coleccion;
import ar.edu.utn.frba.dds.domain.utils.DBUtils;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.Optional;

public class ColeccionRepository {
  private final EntityManager em = DBUtils.getEntityManager();

  public ColeccionRepository() {
  }

  public void save(Coleccion coleccion) {
    if (coleccion == null) return;
    DBUtils.comenzarTransaccion(em);
    // Usamos el método centralizado para enriquecer cada hecho.
    coleccion.getHechosConsensuados()
             .forEach(DBUtils::enriquecerHecho);
    em.persist(coleccion);
    DBUtils.commit(em);
  }

  public List<Coleccion> findAll() {
    return em.createQuery("SELECT c FROM Coleccion c", Coleccion.class)
             .getResultList();
  }

  public Optional<Coleccion> findById(Long id) {
    try {
      Coleccion coleccion = em.createQuery("SELECT c FROM Coleccion c WHERE c.id = :id", Coleccion.class)
                              .setParameter("id", id)
                              .getSingleResult();
      return Optional.of(coleccion);
    } catch (NoResultException e) {
      return Optional.empty();
    }
  }
}
