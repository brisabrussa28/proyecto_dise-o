package ar.edu.utn.frba.dds.repositories;

import ar.edu.utn.frba.dds.model.hecho.Hecho;
import ar.edu.utn.frba.dds.utils.DBUtils;
import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;

public class HechoRepository {
  private final EntityManager em = DBUtils.getEntityManager();

  private static final HechoRepository INSTANCE = new HechoRepository();

  public static HechoRepository instance() {
    return INSTANCE;
  }

  public void save(Hecho hecho) {
    DBUtils.comenzarTransaccion(em);
    DBUtils.enriquecerHecho(hecho);
    if (hecho.getId() == null) {
      em.persist(hecho);
    } else {
      em.merge(hecho);
    }
    DBUtils.commit(em);
  }

  public List<Hecho> findAll() {
    return em.createQuery("SELECT h FROM Hecho h left join fetch h.fotos", Hecho.class)
             .getResultList();
  }

  public Optional<Hecho> findAny() {
    return this.findAll()
               .stream()
               .findAny();
  }

  public Hecho findById(Long id) {
    return em.createQuery("SELECT h FROM Hecho h JOIN FETCH h.fotos WHERE h.id = :id", Hecho.class)
             .setParameter("id", id)
             .getSingleResult();
  }

  public List<String> getCategorias() {
    return em.createQuery("select distinct h.hecho_categoria from Hecho h", String.class)
             .getResultList();
  }

  public List<String> getEtiquetas() {
    return em.createNativeQuery("SELECT DISTINCT etiqueta_nombre FROM hecho_etiquetas")
             .getResultList();
  }

  public Long countAll() {
    return em.createQuery("SELECT COUNT(DISTINCT h.id) FROM Hecho h", Long.class)
             .getSingleResult();
  }

}