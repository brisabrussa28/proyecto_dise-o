package ar.edu.utn.frba.dds.model.lector.csv.filaconverter.repositories;

import ar.edu.utn.frba.dds.model.coleccion.Coleccion;
import ar.edu.utn.frba.dds.model.filtro.condiciones.Condicion;
import ar.edu.utn.frba.dds.model.filtro.condiciones.CondicionCompuesta;
import ar.edu.utn.frba.dds.model.fuentes.Fuente;
import ar.edu.utn.frba.dds.model.fuentes.FuenteConHechos;
import ar.edu.utn.frba.dds.model.fuentes.FuenteDeAgregacion;
import ar.edu.utn.frba.dds.model.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.model.hecho.Hecho;
import ar.edu.utn.frba.dds.utils.DBUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

public class ColeccionRepository {
  private static final ColeccionRepository INSTANCE = new ColeccionRepository();

  public static ColeccionRepository instance() {
    return INSTANCE;
  }

  public void save(Coleccion coleccion) {
    EntityManager em = DBUtils.getEntityManager();

    DBUtils.comenzarTransaccion(em);
    try {
      if (coleccion.getId() == null) {
        em.persist(coleccion);
      } else {
        coleccion = em.merge(coleccion);
      }

      if (coleccion.getHechos() != null) {
        for (Hecho h : coleccion.getHechos()) {
          DBUtils.enriquecerHecho(h);
          if (h.getId() == null) {
            em.persist(h);
          } else {
            em.merge(h);
          }
        }
      }

      DBUtils.commit(em);
    } catch (PersistenceException e) {
      DBUtils.rollback(em);
      throw new RuntimeException("Error al guardar colección: " + e.getMessage(), e);
    } finally {
      em.close();
    }
  }

  public List<Coleccion> findAll() {
    EntityManager em = DBUtils.getEntityManager();
    try {
      return em.createQuery("SELECT DISTINCT c FROM Coleccion c LEFT JOIN FETCH c.coleccion_fuente", Coleccion.class)
               .getResultList();
    } finally {
      em.close();
    }
  }

  public Coleccion findById(Long id) {
    EntityManager em = DBUtils.getEntityManager();
    try {
      Coleccion coleccion = em.createQuery(
                                  "SELECT DISTINCT c FROM Coleccion c "
                                      + "LEFT JOIN FETCH c.coleccion_fuente "
                                      + "LEFT JOIN FETCH c.coleccion_algoritmo "
                                      + "LEFT JOIN FETCH c.coleccion_condicion "
                                      + "LEFT JOIN FETCH c.hechos "
                                      + "WHERE c.coleccion_id = :id",
                                  Coleccion.class
                              )
                              .setParameter("id", id)
                              .getSingleResult();

      if (coleccion.getFuente() != null) {
        cargarHechosParaFuente(em, coleccion.getFuente());
      }

      // Inicialización robusta del árbol de condiciones
      if (coleccion.getCondicion() != null) {
        inicializarCondicionRecursiva(coleccion.getCondicion());
      }

      return coleccion;
    } catch (NoResultException e) {
      return null;
    } finally {
      em.close();
    }
  }

  // Helper para inicializar el árbol de condiciones (Composite Pattern)
  private void inicializarCondicionRecursiva(Condicion condicion) {
    if (condicion == null) return;

    // Eliminada la llamada a condicion.getId() que causaba error.
    // La inicialización importante es la de las listas hijas en CondicionCompuesta.

    if (condicion instanceof CondicionCompuesta) {
      CondicionCompuesta compuesta = (CondicionCompuesta) condicion;
      if (compuesta.getCondiciones() != null) {
        // Inicializar la colección Lazy accediendo a su tamaño
        compuesta.getCondiciones().size();

        // Recursión para inicializar los nietos/bisnietos
        for (Condicion hija : compuesta.getCondiciones()) {
          inicializarCondicionRecursiva(hija);
        }
      }
    }
  }

  public List<Coleccion> buscarRapido(String titulo, String categoria) {
    EntityManager em = DBUtils.getEntityManager();
    try {
      StringBuilder queryStr = new StringBuilder(
          "SELECT DISTINCT c.* FROM coleccion c WHERE 1=1"
      );

      Map<String, Object> params = new HashMap<>();
      int UMBRAL_TOLERANCIA = 3;

      if (titulo != null && !titulo.isBlank()) {
        queryStr.append(" AND (");
        queryStr.append("   levenshtein(unaccent(LOWER(COALESCE(c.coleccion_titulo, ''))), unaccent(LOWER(:tituloRaw))) <= :umbral");
        queryStr.append("   OR unaccent(LOWER(COALESCE(c.coleccion_titulo, ''))) LIKE unaccent(LOWER(:tituloLike))");
        queryStr.append("   OR unaccent(LOWER(COALESCE(c.coleccion_descripcion, ''))) LIKE unaccent(LOWER(:tituloLike))");
        queryStr.append(" )");

        params.put("tituloRaw", titulo.trim());
        params.put("tituloLike", "%" + titulo.trim() + "%");
        params.put("umbral", UMBRAL_TOLERANCIA);
      }

      if (categoria != null && !categoria.equals("Todas") && !categoria.equals("0")) {
        queryStr.append(" AND LOWER(c.coleccion_categoria) = LOWER(:categoriaRaw)");
        params.put("categoriaRaw", categoria.trim());
      }

      queryStr.append(" ORDER BY c.coleccion_titulo DESC");

      Query query = em.createNativeQuery(queryStr.toString(), Coleccion.class);

      for (Map.Entry<String, Object> entry : params.entrySet()) {
        query.setParameter(entry.getKey(), entry.getValue());
      }

      List<Coleccion> resultados = query.getResultList();

      for (Coleccion col : resultados) {
        if (col.getHechos() != null) {
          col.getHechos().size();
        }

        if (col.getFuente() instanceof FuenteDinamica) {
          FuenteDinamica fd = (FuenteDinamica) col.getFuente();
          if (fd.getHechos() != null) fd.getHechos().size();
        }
      }

      return resultados;

    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    } finally {
      em.close();
    }
  }

  private void cargarHechosParaFuente(EntityManager em, Fuente fuente) {
    if (fuente == null) return;

    try {
      if (fuente instanceof FuenteDeAgregacion) {
        FuenteDeAgregacion ag = em.find(FuenteDeAgregacion.class, fuente.getId());
        if(ag != null) ag.getFuentesCargadas().size();
      } else if (fuente instanceof FuenteConHechos) {
        FuenteConHechos fch = em.find(FuenteConHechos.class, fuente.getId());
        if(fch != null) fch.getHechos().size();
      }
    } catch (Exception e) {
      System.err.println("Advertencia: No se pudieron cargar hechos para fuente en ColeccionRepo: " + e.getMessage());
    }
  }

  public List<Coleccion> findAllConFuentesYHechos() {
    EntityManager em = DBUtils.getEntityManager();
    try {
      List<Coleccion> colecciones = em.createQuery(
                                          "SELECT DISTINCT c FROM Coleccion c " +
                                              "LEFT JOIN FETCH c.coleccion_fuente f " +
                                              "WHERE f IS NOT NULL", Coleccion.class)
                                      .getResultList();

      for (Coleccion coleccion : colecciones) {
        if (coleccion.getFuente() != null) {
          cargarHechosParaFuente(em, coleccion.getFuente());
        }
      }

      return colecciones;
    } finally {
      em.close();
    }
  }

  public List<Coleccion> findByFuenteId(Long fuenteId) {
    EntityManager em = DBUtils.getEntityManager();
    try {
      return em.createQuery(
                   "SELECT c FROM Coleccion c WHERE c.coleccion_fuente.id = :fuenteId",
                   Coleccion.class)
               .setParameter("fuenteId", fuenteId)
               .getResultList();
    } finally {
      em.close();
    }
  }

  public void delete(Coleccion coleccion) {
    EntityManager em = DBUtils.getEntityManager();
    DBUtils.comenzarTransaccion(em);
    try {
      Coleccion managed = em.contains(coleccion) ? coleccion : em.merge(coleccion);
      em.remove(managed);
      DBUtils.commit(em);
    } catch (Exception e) {
      DBUtils.rollback(em);
      throw new RuntimeException("Error al eliminar colección", e);
    } finally {
      em.close();
    }
  }

  public Long countAll() {
    EntityManager em = DBUtils.getEntityManager();
    try {
      return em.createQuery(
                   "SELECT COUNT(DISTINCT c.coleccion_id) FROM Coleccion c",
                   Long.class)
               .getSingleResult();
    } finally {
      em.close();
    }
  }

  public List<String> getCategorias() {
    EntityManager em = DBUtils.getEntityManager();
    try {
      return em.createQuery(
                   "SELECT DISTINCT c.coleccion_categoria FROM Coleccion c",
                   String.class)
               .getResultList();
    } finally {
      em.close();
    }
  }

  public List<Coleccion> findAllConHechos() {
    EntityManager em = DBUtils.getEntityManager();
    try {
      return em.createQuery(
                   "SELECT DISTINCT c FROM Coleccion c " +
                       "LEFT JOIN FETCH c.coleccion_fuente f " +
                       "LEFT JOIN FETCH c.hechos h " +
                       "LEFT JOIN FETCH h.etiquetas " +
                       "LEFT JOIN FETCH h.fotos",
                   Coleccion.class)
               .getResultList();
    } finally {
      em.close();
    }
  }

  public Coleccion findByIdConHechosInicializados(Long id) {
    EntityManager em = DBUtils.getEntityManager();
    try {
      Coleccion col = em.createQuery(
                            "SELECT DISTINCT c FROM Coleccion c " +
                                "LEFT JOIN FETCH c.hechos h " +
                                "WHERE c.coleccion_id = :id", Coleccion.class)
                        .setParameter("id", id)
                        .getSingleResult();

      if(col.getFuente() != null) {
        cargarHechosParaFuente(em, col.getFuente());
      }
      if (col.getCondicion() != null) {
        inicializarCondicionRecursiva(col.getCondicion());
      }

      return col;
    } catch (NoResultException e) {
      return null;
    } finally {
      em.close();
    }
  }

  public Coleccion findColeccionByTitulo(String tituloColeccion) {
    EntityManager em = DBUtils.getEntityManager();
    try {
      return em.createQuery(
                   "SELECT c FROM Coleccion c WHERE c.coleccion_titulo = :titulo",
                   Coleccion.class)
               .setParameter("titulo", tituloColeccion)
               .getSingleResult();
    } catch (NoResultException e) {
      return null;
    } finally {
      em.close();
    }
  }

  public List<Hecho> findHechosConsensuados() {
    EntityManager em = DBUtils.getEntityManager();
    try {
      return em.createQuery(
                   "SELECT DISTINCT h FROM Coleccion c JOIN c.hechos h " +
                       "LEFT JOIN FETCH h.fotos " +
                       "LEFT JOIN FETCH h.etiquetas",
                   Hecho.class)
               .getResultList();
    } finally {
      em.close();
    }
  }
}