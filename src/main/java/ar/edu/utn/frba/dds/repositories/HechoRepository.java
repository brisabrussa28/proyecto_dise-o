package ar.edu.utn.frba.dds.repositories;

import ar.edu.utn.frba.dds.model.hecho.Hecho;
import ar.edu.utn.frba.dds.utils.DBUtils;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

public class HechoRepository {

  private static final HechoRepository INSTANCE = new HechoRepository();

  public static HechoRepository instance() {
    return INSTANCE;
  }

  // Thread-local para almacenar el total de resultados de la última búsqueda paginada
  private static final ThreadLocal<Long> TOTAL_RESULTS_THREAD_LOCAL = new ThreadLocal<>();

  public void save(Hecho hecho) {
    EntityManager em = DBUtils.getEntityManager();
    DBUtils.comenzarTransaccion(em);
    try {
      DBUtils.enriquecerHecho(hecho);

      if (hecho.getId() == null) {
        em.persist(hecho);
      } else {
        em.merge(hecho);
      }
      DBUtils.commit(em);
    } catch (PersistenceException e) {
      DBUtils.rollback(em);
      throw new RuntimeException("Error al guardar hecho: " + e.getMessage());
    } finally {
      em.close();
    }
  }

  public List<Hecho> findAll() {
    EntityManager em = DBUtils.getEntityManager();
    try {
      return em.createQuery(
                   "SELECT DISTINCT h FROM Hecho h " +
                       "LEFT JOIN FETCH h.fotos " +
                       "LEFT JOIN FETCH h.etiquetas " +
                       "LEFT JOIN FETCH h.colecciones",
                   Hecho.class)
               .getResultList();
    } finally {
      em.close();
    }
  }

  public Optional<Hecho> findAny() {
    return this.findAll()
               .stream()
               .findAny();
  }

  public Hecho findById(Long id) {
    EntityManager em = DBUtils.getEntityManager();
    try {
      return em.createQuery(
                   "SELECT h FROM Hecho h " +
                       "LEFT JOIN FETCH h.fotos " +
                       "LEFT JOIN FETCH h.etiquetas " +
                       "WHERE h.id = :id", Hecho.class)
               .setParameter("id", id)
               .getSingleResult();
    } finally {
      em.close();
    }
  }

  // Búsqueda simple por título (para compatibilidad con código existente)
  public List<Hecho> findByTitle(String titulo) {
    EntityManager em = DBUtils.getEntityManager();

    int UMBRAL_TOLERANCIA = 3;

    String sql = "SELECT * FROM Hecho h " +
        "WHERE levenshtein(unaccent(LOWER(h.hecho_titulo)), unaccent(LOWER(:queryParam))) <= :umbral " +
        "   OR unaccent(LOWER(h.hecho_descripcion)) LIKE unaccent(LOWER(:queryParamLike))";

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

  // BÚSQUEDA AVANZADA COMPLETA - Método principal
  public List<Hecho> buscarAvanzadoCompleto(
      String titulo,
      String categoria,
      String fuente,
      String coleccion,
      LocalDate fechaDesde,
      LocalDate fechaHasta,
      Boolean soloConsensuados) {

    EntityManager em = DBUtils.getEntityManager();
    try {
      StringBuilder queryStr = new StringBuilder(
          "SELECT DISTINCT h FROM Hecho h " +
              "LEFT JOIN FETCH h.fotos " +
              "LEFT JOIN FETCH h.etiquetas " +
              "LEFT JOIN h.fuente f " +
              "WHERE 1=1"
      );

      Map<String, Object> params = new HashMap<>();

      // Filtro por título (contains)
      if (titulo != null && !titulo.isBlank()) {
        queryStr.append(" AND LOWER(h.hecho_titulo) LIKE LOWER(:titulo)");
        params.put("titulo", "%" + titulo + "%");
      }

      // Filtro por categoría
      if (categoria != null && !categoria.isBlank() && !categoria.equals("0")) {
        queryStr.append(" AND LOWER(h.hecho_categoria) = LOWER(:categoria)");
        params.put("categoria", categoria);
      }

      // Filtro por fuente (a través de la relación con fuente)
      if (fuente != null && !fuente.isBlank() && !fuente.equals("0")) {
        queryStr.append(" AND (f.nombre = :fuenteNombre OR f.nombre LIKE :fuenteLike)");
        params.put("fuenteNombre", fuente);
        params.put("fuenteLike", "%" + fuente + "%");
      }

      // Filtro por colección (hechos que pertenecen a una colección)
      if (coleccion != null && !coleccion.isBlank() && !coleccion.equals("0")) {
        queryStr.append(" AND h.id IN (SELECT hc.id FROM Coleccion c JOIN c.hechosConsensuados hc WHERE c.titulo = :coleccionTitulo)");
        params.put("coleccionTitulo", coleccion);
      }

      // Filtro por rango de fechas
      if (fechaDesde != null) {
        queryStr.append(" AND h.hecho_fecha_suceso >= :fechaDesde");
        params.put("fechaDesde", fechaDesde.atStartOfDay());
      }

      if (fechaHasta != null) {
        queryStr.append(" AND h.hecho_fecha_suceso <= :fechaHasta");
        params.put("fechaHasta", fechaHasta.atTime(23, 59, 59));
      }

      // Filtro por hechos consensuados
      if (soloConsensuados != null && soloConsensuados) {
        queryStr.append(" AND h.id IN (SELECT hc.id FROM Coleccion c JOIN c.hechosConsensuados hc)");
      }

      // Ordenar por fecha más reciente
      queryStr.append(" ORDER BY h.hecho_fecha_suceso DESC");

      TypedQuery<Hecho> query = em.createQuery(queryStr.toString(), Hecho.class);

      for (Map.Entry<String, Object> entry : params.entrySet()) {
        query.setParameter(entry.getKey(), entry.getValue());
      }

      return query.getResultList();
    } finally {
      em.close();
    }
  }

  // BÚSQUEDA AVANZADA PAGINADA - Para listados grandes
  public Map<String, Object> buscarAvanzadoCompletoPaginated(
      String titulo,
      String categoria,
      String fuente,
      String coleccion,
      LocalDate fechaDesde,
      LocalDate fechaHasta,
      Boolean soloConsensuados,
      int page,
      int pageSize) {

    EntityManager em = DBUtils.getEntityManager();
    try {
      // Query para resultados
      StringBuilder queryStr = new StringBuilder(
          "SELECT DISTINCT h FROM Hecho h " +
              "LEFT JOIN FETCH h.fotos " +
              "LEFT JOIN FETCH h.etiquetas " +
              "LEFT JOIN h.fuente f " +
              "WHERE 1=1"
      );

      // Query para contar total
      StringBuilder countQueryStr = new StringBuilder(
          "SELECT COUNT(DISTINCT h) FROM Hecho h " +
              "LEFT JOIN h.fuente f " +
              "WHERE 1=1"
      );

      Map<String, Object> params = new HashMap<>();

      // Aplicar los mismos filtros a ambas queries
      if (titulo != null && !titulo.isBlank()) {
        queryStr.append(" AND LOWER(h.hecho_titulo) LIKE LOWER(:titulo)");
        countQueryStr.append(" AND LOWER(h.hecho_titulo) LIKE LOWER(:titulo)");
        params.put("titulo", "%" + titulo + "%");
      }

      if (categoria != null && !categoria.isBlank() && !categoria.equals("0")) {
        queryStr.append(" AND LOWER(h.hecho_categoria) = LOWER(:categoria)");
        countQueryStr.append(" AND LOWER(h.hecho_categoria) = LOWER(:categoria)");
        params.put("categoria", categoria);
      }

      if (fuente != null && !fuente.isBlank() && !fuente.equals("0")) {
        queryStr.append(" AND (f.nombre = :fuenteNombre OR f.nombre LIKE :fuenteLike)");
        countQueryStr.append(" AND (f.nombre = :fuenteNombre OR f.nombre LIKE :fuenteLike)");
        params.put("fuenteNombre", fuente);
        params.put("fuenteLike", "%" + fuente + "%");
      }

      if (coleccion != null && !coleccion.isBlank() && !coleccion.equals("0")) {
        queryStr.append(" AND h.id IN (SELECT hc.id FROM Coleccion c JOIN c.hechosConsensuados hc WHERE c.titulo = :coleccionTitulo)");
        countQueryStr.append(" AND h.id IN (SELECT hc.id FROM Coleccion c JOIN c.hechosConsensuados hc WHERE c.titulo = :coleccionTitulo)");
        params.put("coleccionTitulo", coleccion);
      }

      if (fechaDesde != null) {
        queryStr.append(" AND h.hecho_fecha_suceso >= :fechaDesde");
        countQueryStr.append(" AND h.hecho_fecha_suceso >= :fechaDesde");
        params.put("fechaDesde", fechaDesde.atStartOfDay());
      }

      if (fechaHasta != null) {
        queryStr.append(" AND h.hecho_fecha_suceso <= :fechaHasta");
        countQueryStr.append(" AND h.hecho_fecha_suceso <= :fechaHasta");
        params.put("fechaHasta", fechaHasta.atTime(23, 59, 59));
      }

      if (soloConsensuados != null && soloConsensuados) {
        queryStr.append(" AND h.id IN (SELECT hc.id FROM Coleccion c JOIN c.hechosConsensuados hc)");
        countQueryStr.append(" AND h.id IN (SELECT hc.id FROM Coleccion c JOIN c.hechosConsensuados hc)");
      }

      // Ordenar por fecha
      queryStr.append(" ORDER BY h.hecho_fecha_suceso DESC");

      // Contar total
      TypedQuery<Long> countQuery = em.createQuery(countQueryStr.toString(), Long.class);
      for (Map.Entry<String, Object> entry : params.entrySet()) {
        countQuery.setParameter(entry.getKey(), entry.getValue());
      }
      Long total = countQuery.getSingleResult();

      // Obtener resultados paginados
      TypedQuery<Hecho> query = em.createQuery(queryStr.toString(), Hecho.class);
      for (Map.Entry<String, Object> entry : params.entrySet()) {
        query.setParameter(entry.getKey(), entry.getValue());
      }

      query.setFirstResult((page - 1) * pageSize);
      query.setMaxResults(pageSize);

      List<Hecho> resultados = query.getResultList();

      Map<String, Object> response = new HashMap<>();
      response.put("resultados", resultados);
      response.put("total", total);
      response.put("paginaActual", page);
      response.put("totalPaginas", (int) Math.ceil((double) total / pageSize));

      return response;

    } finally {
      em.close();
    }
  }

  // BÚSQUEDA RÁPIDA - Para el mapa principal (solo título y consensuados)
  public List<Hecho> buscarRapido(String titulo, Boolean soloConsensuados) {
    EntityManager em = DBUtils.getEntityManager();
    try {
      StringBuilder queryStr = new StringBuilder(
          "SELECT DISTINCT h.* FROM Hecho h " +
              "WHERE 1=1"
      );

      Map<String, Object> params = new HashMap<>();
      int UMBRAL_TOLERANCIA = 3;

      if (titulo != null && !titulo.isBlank()) {
        queryStr.append(" AND (");
        queryStr.append("   levenshtein(unaccent(LOWER(h.hecho_titulo)), unaccent(LOWER(:tituloRaw))) <= :umbral");
        queryStr.append("   OR unaccent(LOWER(h.hecho_descripcion)) LIKE unaccent(LOWER(:tituloLike))");
        queryStr.append(" )");

        params.put("tituloRaw", titulo.trim());
        params.put("tituloLike", "%" + titulo.trim() + "%");
        params.put("umbral", UMBRAL_TOLERANCIA);
      }

      if (soloConsensuados != null && soloConsensuados) {
        queryStr.append(" AND h.id IN (SELECT hc.hecho_id FROM Coleccion_Hecho hc)");
      }

      queryStr.append(" ORDER BY h.hecho_fecha_suceso DESC");

      Query query = em.createNativeQuery(queryStr.toString(), Hecho.class);

      for (Map.Entry<String, Object> entry : params.entrySet()) {
        query.setParameter(entry.getKey(), entry.getValue());
      }

      return query.getResultList();
    } finally {
      em.close();
    }
  }


  // Métodos auxiliares para obtener información de filtros
  public List<String> getCategorias() {
    EntityManager em = DBUtils.getEntityManager();
    try {
      return em.createQuery(
                   "SELECT DISTINCT h.hecho_categoria FROM Hecho h WHERE h.hecho_categoria IS NOT NULL ORDER BY h.hecho_categoria", String.class)
               .getResultList();
    } finally {
      em.close();
    }
  }

  public List<String> getFuentesDisponibles() {
    EntityManager em = DBUtils.getEntityManager();
    try {
      return em.createQuery(
                   "SELECT DISTINCT f.nombre FROM Fuente f WHERE f.nombre IS NOT NULL ORDER BY f.nombre", String.class)
               .getResultList();
    } finally {
      em.close();
    }
  }

  public List<String> getColeccionesDisponibles() {
    EntityManager em = DBUtils.getEntityManager();
    try {
      return em.createQuery(
                   "SELECT DISTINCT c.titulo FROM Coleccion c WHERE c.titulo IS NOT NULL ORDER BY c.titulo", String.class)
               .getResultList();
    } finally {
      em.close();
    }
  }

  public List<String> getEtiquetas() {
    EntityManager em = DBUtils.getEntityManager();
    try {
      return em.createNativeQuery(
                   "SELECT DISTINCT etiqueta_nombre FROM hecho_etiquetas ORDER BY etiqueta_nombre")
               .getResultList();
    } finally {
      em.close();
    }
  }

  public Long countAll() {
    EntityManager em = DBUtils.getEntityManager();
    try {
      return em.createQuery(
                   "SELECT COUNT(DISTINCT h.id) FROM Hecho h", Long.class)
               .getSingleResult();
    } finally {
      em.close();
    }
  }

  public Long countByFiltros(String categoria, LocalDate fechaDesde, LocalDate fechaHasta) {
    EntityManager em = DBUtils.getEntityManager();
    try {
      StringBuilder queryStr = new StringBuilder(
          "SELECT COUNT(DISTINCT h.id) FROM Hecho h WHERE 1=1"
      );

      Map<String, Object> params = new HashMap<>();

      if (categoria != null && !categoria.isBlank() && !categoria.equals("0")) {
        queryStr.append(" AND LOWER(h.hecho_categoria) = LOWER(:categoria)");
        params.put("categoria", categoria);
      }

      if (fechaDesde != null) {
        queryStr.append(" AND h.hecho_fecha_suceso >= :fechaDesde");
        params.put("fechaDesde", fechaDesde.atStartOfDay());
      }

      if (fechaHasta != null) {
        queryStr.append(" AND h.hecho_fecha_suceso <= :fechaHasta");
        params.put("fechaHasta", fechaHasta.atTime(23, 59, 59));
      }

      TypedQuery<Long> query = em.createQuery(queryStr.toString(), Long.class);

      for (Map.Entry<String, Object> entry : params.entrySet()) {
        query.setParameter(entry.getKey(), entry.getValue());
      }

      return query.getSingleResult();
    } finally {
      em.close();
    }
  }

  // Método para obtener el total de la última búsqueda paginada
  public static Long getLastTotalResults() {
    Long total = TOTAL_RESULTS_THREAD_LOCAL.get();
    TOTAL_RESULTS_THREAD_LOCAL.remove();
    return total != null ? total : 0L;
  }

  // Método auxiliar para configurar el thread-local
  private static void setLastTotalResults(Long total) {
    TOTAL_RESULTS_THREAD_LOCAL.set(total);
  }
}