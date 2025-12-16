package ar.edu.utn.frba.dds.repositories;

import ar.edu.utn.frba.dds.model.coleccion.Coleccion;
import ar.edu.utn.frba.dds.model.fuentes.Fuente;
import ar.edu.utn.frba.dds.model.fuentes.FuenteConHechos;
import ar.edu.utn.frba.dds.model.fuentes.FuenteDeAgregacion;
import ar.edu.utn.frba.dds.model.hecho.Estado;
import ar.edu.utn.frba.dds.model.hecho.Hecho;
import ar.edu.utn.frba.dds.utils.DBUtils;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

public class HechoRepository {

  private static final HechoRepository INSTANCE = new HechoRepository();

  public static HechoRepository instance() {
    return INSTANCE;
  }

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
      throw new RuntimeException("Error al guardar hecho: " + e.getMessage(), e);
    } finally {
      em.close();
    }
  }

  // --- LÓGICA DE INTEGRIDAD: Colección -> Fuente -> Hecho ---

  /**
   * Obtiene los IDs de todas las FuentesConHechos (hojas) que son alcanzables
   * desde las colecciones activas.
   */
  private Set<Long> obtenerIdsFuentesValidas(EntityManager em) {
    List<Fuente> fuentesRaiz = em.createQuery(
                                     "SELECT c.coleccion_fuente FROM Coleccion c WHERE c.coleccion_fuente IS NOT NULL",
                                     Fuente.class
                                 )
                                 .getResultList();

    Set<Long> idsValidos = new HashSet<>();
    Set<Long> visitados = new HashSet<>();

    for (Fuente f : fuentesRaiz) {
      recolectarIdsFuentes(f, idsValidos, visitados, em);
    }

    return idsValidos;
  }

  private void recolectarIdsFuentes(
      Fuente fuente,
      Set<Long> ids,
      Set<Long> visitados,
      EntityManager em
  ) {
    if (fuente == null || visitados.contains(fuente.getId())) {
      return;
    }
    visitados.add(fuente.getId());

    if (fuente instanceof FuenteConHechos) {
      ids.add(fuente.getId());
    } else if (fuente instanceof FuenteDeAgregacion) {
      // Usamos una query nativa o simple para evitar problemas de proxy con Hibernate
      List<Fuente> hijos = em.createQuery(
                                 "SELECT child FROM FuenteDeAgregacion p JOIN p.fuentesCargadas child WHERE p.id = :id",
                                 Fuente.class
                             )
                             .setParameter("id", fuente.getId())
                             .getResultList();

      for (Fuente hijo : hijos) {
        recolectarIdsFuentes(hijo, ids, visitados, em);
      }
    }
  }

  public List<Hecho> findAll() {
    EntityManager em = DBUtils.getEntityManager();
    try {
      // CORRECCIÓN CRÍTICA: findAll trae TODO (sin filtro de integridad).
      // Esto previene que la app parezca vacía si no hay colecciones configuradas.
      return em.createQuery(
                   "SELECT DISTINCT h FROM Hecho h " +
                       "LEFT JOIN FETCH h.fotos " +
                       "LEFT JOIN FETCH h.etiquetas " +
                       "LEFT JOIN FETCH h.colecciones",
                   Hecho.class
               )
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
                       "LEFT JOIN FETCH h.colecciones " +
                       "WHERE h.id = :id", Hecho.class
               )
               .setParameter("id", id)
               .getSingleResult();
    } finally {
      em.close();
    }
  }

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
    } finally {
      em.close();
    }
  }

  public List<Hecho> buscarAvanzadoCompleto(
      String titulo,
      String categoria,
      String fuente,
      String coleccion,
      LocalDate fechaDesde,
      LocalDate fechaHasta,
      Boolean soloConsensuados,
      Boolean incluirEliminados
  ) {

    EntityManager em = DBUtils.getEntityManager();
    try {
      Set<Long> idsFuentesValidas = obtenerIdsFuentesValidas(em);

      // Filtro Colección -> Fuente
      if (coleccion != null && !coleccion.isBlank() && !coleccion.equals("0")) {
        try {
          Fuente fuenteColeccion = em.createQuery(
                                         "SELECT c.coleccion_fuente FROM Coleccion c WHERE c.coleccion_titulo = :titulo",
                                         Fuente.class
                                     )
                                     .setParameter("titulo", coleccion)
                                     .getSingleResult();

          Set<Long> idsFuenteEspecifica = new HashSet<>();
          recolectarIdsFuentes(fuenteColeccion, idsFuenteEspecifica, new HashSet<>(), em);
          idsFuentesValidas.retainAll(idsFuenteEspecifica);

        } catch (javax.persistence.NoResultException e) {
          return Collections.emptyList();
        }
      }

      // Filtro Fuente Directa
      if (fuente != null && !fuente.isBlank() && !fuente.equals("0")) {
        try {
          Fuente fuenteObj = em.createQuery(
                                   "SELECT f FROM Fuente f WHERE f.fuente_nombre = :nombre", Fuente.class)
                               .setParameter("nombre", fuente)
                               .getSingleResult();

          Set<Long> idsFuenteSeleccionada = new HashSet<>();
          recolectarIdsFuentes(fuenteObj, idsFuenteSeleccionada, new HashSet<>(), em);
          idsFuentesValidas.retainAll(idsFuenteSeleccionada);

        } catch (javax.persistence.NoResultException e) {
          return Collections.emptyList();
        }
      }

      if (idsFuentesValidas.isEmpty()) {
        return Collections.emptyList();
      }

      StringBuilder queryStr = new StringBuilder(
          "SELECT DISTINCT h FROM Hecho h " +
              "LEFT JOIN FETCH h.fotos " +
              "LEFT JOIN FETCH h.etiquetas " +
              "LEFT JOIN FETCH h.colecciones " +
              "WHERE 1=1"
      );

      // Integridad
      queryStr.append(
          " AND h.id IN (SELECT fh.id FROM FuenteConHechos f JOIN f.hechos fh WHERE f.id IN :idsFuentesValidas)");

      Map<String, Object> params = new HashMap<>();
      params.put("idsFuentesValidas", idsFuentesValidas);

      // --- LÓGICA DE SEGURIDAD PARA ELIMINADOS ---
      // Si incluirEliminados es null o false, filtramos los que tengan estado ELIMINADO
      if (incluirEliminados == null || !incluirEliminados) {
        queryStr.append(" AND h.estado <> :estadoEliminado");
        params.put("estadoEliminado", Estado.ELIMINADO);
      }
      // ------------------------------------------

      if (titulo != null && !titulo.isBlank()) {
        queryStr.append(" AND LOWER(h.hecho_titulo) LIKE LOWER(:titulo)");
        params.put("titulo", "%" + titulo + "%");
      }

      if (categoria != null && !categoria.isBlank() && !categoria.equals("0")) {
        queryStr.append(" AND LOWER(h.hecho_categoria) = LOWER(:categoria)");
        params.put("categoria", categoria);
      }

      if (soloConsensuados != null && soloConsensuados) {
        queryStr.append(" AND h IN (SELECT hc FROM Coleccion c JOIN c.hechos hc)");
      }

      if (fechaDesde != null) {
        queryStr.append(" AND h.hecho_fecha_suceso >= :fechaDesde");
        params.put("fechaDesde", fechaDesde.atStartOfDay());
      }

      if (fechaHasta != null) {
        queryStr.append(" AND h.hecho_fecha_suceso <= :fechaHasta");
        params.put("fechaHasta", fechaHasta.atTime(23, 59, 59));
      }

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

  public Map<String, Object> buscarAvanzadoCompletoPaginated(
      String titulo,
      String categoria,
      String fuente,
      String coleccion,
      LocalDate fechaDesde,
      LocalDate fechaHasta,
      Boolean soloConsensuados,
      Boolean incluirEliminados, // <--- NUEVO PARÁMETRO
      int page,
      int pageSize
  ) {

    EntityManager em = DBUtils.getEntityManager();
    try {
      Set<Long> idsFuentesValidas = obtenerIdsFuentesValidas(em);

      if (coleccion != null && !coleccion.isBlank() && !coleccion.equals("0")) {
        try {
          Fuente fuenteColeccion = em.createQuery(
                                         "SELECT c.coleccion_fuente FROM Coleccion c WHERE c.coleccion_titulo = :titulo",
                                         Fuente.class
                                     )
                                     .setParameter("titulo", coleccion)
                                     .getSingleResult();

          Set<Long> idsFuenteEspecifica = new HashSet<>();
          recolectarIdsFuentes(fuenteColeccion, idsFuenteEspecifica, new HashSet<>(), em);
          idsFuentesValidas.retainAll(idsFuenteEspecifica);

        } catch (javax.persistence.NoResultException e) {
          return Map.of(
              "resultados",
              Collections.emptyList(),
              "total",
              0L,
              "paginaActual",
              page,
              "totalPaginas",
              0
          );
        }
      }

      if (fuente != null && !fuente.isBlank() && !fuente.equals("0")) {
        try {
          Fuente fuenteObj = em.createQuery(
                                   "SELECT f FROM Fuente f WHERE f.fuente_nombre = :nombre", Fuente.class)
                               .setParameter("nombre", fuente)
                               .getSingleResult();

          Set<Long> idsFuenteSeleccionada = new HashSet<>();
          recolectarIdsFuentes(fuenteObj, idsFuenteSeleccionada, new HashSet<>(), em);
          idsFuentesValidas.retainAll(idsFuenteSeleccionada);

        } catch (javax.persistence.NoResultException e) {
          return Map.of(
              "resultados",
              Collections.emptyList(),
              "total",
              0L,
              "paginaActual",
              page,
              "totalPaginas",
              0
          );
        }
      }

      if (idsFuentesValidas.isEmpty()) {
        return Map.of(
            "resultados",
            Collections.emptyList(),
            "total",
            0L,
            "paginaActual",
            page,
            "totalPaginas",
            0
        );
      }

      StringBuilder queryStr = new StringBuilder(
          "SELECT DISTINCT h FROM Hecho h " +
              "LEFT JOIN FETCH h.fotos " +
              "LEFT JOIN FETCH h.etiquetas " +
              "LEFT JOIN FETCH h.colecciones " +
              "WHERE 1=1"
      );

      StringBuilder countQueryStr = new StringBuilder(
          "SELECT COUNT(DISTINCT h) FROM Hecho h WHERE 1=1"
      );

      String integridadClause = " AND h.id IN (SELECT fh.id FROM FuenteConHechos f JOIN f.hechos fh WHERE f.id IN :idsFuentesValidas)";
      queryStr.append(integridadClause);
      countQueryStr.append(integridadClause);

      Map<String, Object> params = new HashMap<>();
      params.put("idsFuentesValidas", idsFuentesValidas);

      // --- LÓGICA DE SEGURIDAD PARA ELIMINADOS ---
      if (incluirEliminados == null || !incluirEliminados) {
        String clause = " AND h.estado <> :estadoEliminado";
        queryStr.append(clause);
        countQueryStr.append(clause);
        params.put("estadoEliminado", Estado.ELIMINADO);
      }
      // ------------------------------------------

      if (titulo != null && !titulo.isBlank()) {
        String clause = " AND LOWER(h.hecho_titulo) LIKE LOWER(:titulo)";
        queryStr.append(clause);
        countQueryStr.append(clause);
        params.put("titulo", "%" + titulo + "%");
      }

      if (categoria != null && !categoria.isBlank() && !categoria.equals("0")) {
        String clause = " AND LOWER(h.hecho_categoria) = LOWER(:categoria)";
        queryStr.append(clause);
        countQueryStr.append(clause);
        params.put("categoria", categoria);
      }

      if (fechaDesde != null) {
        String clause = " AND h.hecho_fecha_suceso >= :fechaDesde";
        queryStr.append(clause);
        countQueryStr.append(clause);
        params.put("fechaDesde", fechaDesde.atStartOfDay());
      }

      if (fechaHasta != null) {
        String clause = " AND h.hecho_fecha_suceso <= :fechaHasta";
        queryStr.append(clause);
        countQueryStr.append(clause);
        params.put("fechaHasta", fechaHasta.atTime(23, 59, 59));
      }

      if (soloConsensuados != null && soloConsensuados) {
        String clause = " AND h.id IN (SELECT hc.id FROM Coleccion c JOIN c.hechos hc)";
        queryStr.append(clause);
        countQueryStr.append(clause);
      }

      queryStr.append(" ORDER BY h.hecho_fecha_suceso DESC");

      TypedQuery<Long> countQuery = em.createQuery(countQueryStr.toString(), Long.class);
      for (Map.Entry<String, Object> entry : params.entrySet()) {
        countQuery.setParameter(entry.getKey(), entry.getValue());
      }
      Long total = countQuery.getSingleResult();

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

  public List<Hecho> buscarRapido(String titulo, Boolean soloConsensuados) {
    EntityManager em = DBUtils.getEntityManager();
    try {
      Set<Long> idsFuentesValidas = obtenerIdsFuentesValidas(em);
      if (idsFuentesValidas.isEmpty()) {
        return Collections.emptyList();
      }

      StringBuilder queryStr = new StringBuilder(
          "SELECT DISTINCT h.* FROM Hecho h WHERE 1=1"
      );

      queryStr.append(" AND h.fuente_id IN (:idsFuentes)");

      Map<String, Object> params = new HashMap<>();
      params.put("idsFuentes", idsFuentesValidas);

      int UMBRAL_TOLERANCIA = 3;

      if (titulo != null && !titulo.isBlank()) {
        queryStr.append(" AND (");
        queryStr.append(
            "   levenshtein(unaccent(LOWER(h.hecho_titulo)), unaccent(LOWER(:tituloRaw))) <= :umbral");
        queryStr.append(
            "   OR unaccent(LOWER(h.hecho_descripcion)) LIKE unaccent(LOWER(:tituloLike))");
        queryStr.append(" )");

        params.put("tituloRaw", titulo.trim());
        params.put("tituloLike", "%" + titulo.trim() + "%");
        params.put("umbral", UMBRAL_TOLERANCIA);
      }

      if (soloConsensuados != null && soloConsensuados) {
        queryStr.append(" AND h.hecho_id IN (SELECT hechos_hecho_id FROM coleccion_hecho)");
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

  public List<String> getCategorias() {
    EntityManager em = DBUtils.getEntityManager();
    try {
      return em.createQuery(
                   "SELECT DISTINCT h.hecho_categoria FROM Hecho h WHERE h.hecho_categoria IS NOT NULL ORDER BY h.hecho_categoria",
                   String.class
               )
               .getResultList();
    } finally {
      em.close();
    }
  }

  public List<String> getFuentesDisponibles() {
    EntityManager em = DBUtils.getEntityManager();
    try {
      return em.createQuery(
                   "SELECT DISTINCT f.fuente_nombre FROM Fuente f WHERE f.fuente_nombre IS NOT NULL ORDER BY f.fuente_nombre",
                   String.class
               )
               .getResultList();
    } finally {
      em.close();
    }
  }

  public List<String> getColeccionesDisponibles() {
    EntityManager em = DBUtils.getEntityManager();
    try {
      return em.createQuery(
                   "SELECT DISTINCT c.coleccion_titulo FROM Coleccion c WHERE c.coleccion_titulo IS NOT NULL ORDER BY c.coleccion_titulo",
                   String.class
               )
               .getResultList();
    } finally {
      em.close();
    }
  }

  public List<String> getEtiquetas() {
    EntityManager em = DBUtils.getEntityManager();
    try {
      return em.createNativeQuery(
                   "SELECT DISTINCT etiquetas_etiqueta_nombre FROM Hecho_etiquetas")
               .getResultList();
    } catch (Exception e) {
      return Collections.emptyList();
    } finally {
      em.close();
    }
  }

  public Long countAll() {
    EntityManager em = DBUtils.getEntityManager();
    try {
      // Simplificado: Contar TODOS los hechos del sistema, no solo los "válidos"
      // para que coincida con findAll()
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

  public static Long getLastTotalResults() {
    Long total = TOTAL_RESULTS_THREAD_LOCAL.get();
    TOTAL_RESULTS_THREAD_LOCAL.remove();
    return total != null ? total : 0L;
  }

  // En HechoRepository.java

  public List<Hecho> findAllPublicos() {
    EntityManager em = DBUtils.getEntityManager();
    try {
      return em.createQuery("SELECT h FROM Hecho h WHERE h.estado <> :estadoEliminado", Hecho.class)
               .setParameter("estadoEliminado", Estado.ELIMINADO)
               .getResultList();
    } finally {
      em.close();
    }
  }
}