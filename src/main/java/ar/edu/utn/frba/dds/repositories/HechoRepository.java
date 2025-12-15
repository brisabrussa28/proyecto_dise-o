package ar.edu.utn.frba.dds.repositories;

import ar.edu.utn.frba.dds.model.hecho.Hecho;
import ar.edu.utn.frba.dds.utils.DBUtils;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

public class HechoRepository {

  private static final HechoRepository INSTANCE = new HechoRepository();

  public static HechoRepository instance() {
    return INSTANCE;
  }

  public void save(Hecho hecho) {
    EntityManager em = DBUtils.getEntityManager();
    DBUtils.enriquecerHecho(hecho);
    try {
      DBUtils.comenzarTransaccion(em);
      if (hecho.getId() == null) {
        em.persist(hecho);
      } else {
        em.merge(hecho);
      }
    } catch (PersistenceException e) {
      DBUtils.rollback(em);
      throw new RuntimeException(e.getMessage());
    } finally {
      DBUtils.commit(em);
      em.close();
    }
  }

  public List<Hecho> findAll() {
    EntityManager em = DBUtils.getEntityManager();
    return em.createQuery("SELECT h FROM Hecho h left join fetch h.fotos", Hecho.class)
             .getResultList();
  }

  public Optional<Hecho> findAny() {
    return this.findAll()
               .stream()
               .findAny();
  }

  public Hecho findById(Long id) {
    EntityManager em = DBUtils.getEntityManager();
    return em.createQuery("SELECT h FROM Hecho h lEFT JOIN FETCH h.fotos WHERE h.id = :id", Hecho.class)
             .setParameter("id", id)
             .getSingleResult();
  }

  /*
  public List<Hecho> findByTitle(String titulo) {
    EntityManager em = DBUtils.getEntityManager();

    TypedQuery<Hecho> q = em.createQuery(
        "SELECT h FROM Hecho h LEFT JOIN FETCH h.fotos WHERE LOWER(h.hecho_titulo) LIKE LOWER(:queryParam) OR LOWER(h.hecho_descripcion) LIKE LOWER(:queryParam)",
        Hecho.class
    );

    // Envolvemos el parámetro en "%" para que coincida con cualquier parte del texto
    q.setParameter("queryParam", "%" + titulo + "%");

    return q.getResultList();
  }*/

  public List<Hecho> findByTitle(String titulo) {
    EntityManager em = DBUtils.getEntityManager();

    int UMBRAL_TOLERANCIA = 3;

    String sql = "SELECT * FROM Hecho h " +
        "WHERE levenshtein(LOWER(h.hecho_titulo), LOWER(:queryParam)) <= :umbral " +
        "   OR LOWER(h.hecho_descripcion) LIKE LOWER(:queryParamLike)";

    try {
      Query q = em.createNativeQuery(sql, Hecho.class);

      q.setParameter("queryParam", titulo.trim());
      q.setParameter("umbral", UMBRAL_TOLERANCIA);
      q.setParameter("queryParamLike", "%" + titulo.trim() + "%");

      return q.getResultList();

    } catch (Exception e) {
      e.printStackTrace();
      return Collections.emptyList();
    }
  }

  public List<String> getCategorias() {
    EntityManager em = DBUtils.getEntityManager();
    return em.createQuery("select distinct h.hecho_categoria from Hecho h", String.class)
             .getResultList();
  }

  public List<String> getEtiquetas() {
    EntityManager em = DBUtils.getEntityManager();
    return em.createNativeQuery("SELECT DISTINCT etiqueta_nombre FROM hecho_etiquetas")
             .getResultList();
  }

  public Long countAll() {
    EntityManager em = DBUtils.getEntityManager();
    return em.createQuery("SELECT COUNT(DISTINCT h.id) FROM Hecho h", Long.class)
             .getSingleResult();
  }

}