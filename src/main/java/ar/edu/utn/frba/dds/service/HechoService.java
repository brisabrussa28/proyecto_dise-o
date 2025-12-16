//package ar.edu.utn.frba.dds.service;
//
//import ar.edu.utn.frba.dds.model.hecho.Hecho;
//import ar.edu.utn.frba.dds.repositories.HechoRepository;
//import ar.edu.utn.frba.dds.utils.DBUtils;
//import java.time.LocalDate;
//import java.util.ArrayList;
//import java.util.List;
//import javax.persistence.EntityManager;
//
//public class HechoService {
//
//  public List<Hecho> buscarConFiltros(
//      String titulo,
//      String categoria,
//      String nombreFuente,
//      String tituloColeccion,
//      LocalDate fechaDesde,
//      LocalDate fechaHasta,
//      Boolean soloConsensuados) {
//
//    // Delegamos completamente la búsqueda compleja al repositorio,
//    // que ahora maneja la integridad (Hecho -> Colección) y los JOINs de manera eficiente.
//    // Esto evita traer todos los datos a memoria y filtrarlos manualmente.
//    return HechoRepository.instance().buscarAvanzadoCompleto(
//        titulo,
//        categoria,
//        nombreFuente,
//        tituloColeccion,
//        fechaDesde,
//        fechaHasta,
//        soloConsensuados
//    );
//  }
//
//  /**
//   * Recarga los hechos desde la base de datos utilizando JOIN FETCH.
//   * Esto evita el error LazyInitializationException y reduce drásticamente el número de consultas (N+1).
//   * Nota: Este método se mantiene por compatibilidad si se necesita re-hidratar objetos detached,
//   * pero idealmente el repositorio ya devuelve objetos inicializados.
//   */
//  public List<Hecho> inicializarColecciones(List<Hecho> hechos) {
//    if (hechos == null || hechos.isEmpty()) {
//      return new ArrayList<>();
//    }
//
//    EntityManager em = DBUtils.getEntityManager();
//    List<Hecho> hechosInicializados = new ArrayList<>();
//
//    try {
//      for (Hecho h : hechos) {
//        if (h.getId() != null) {
//          try {
//            // Usamos una query específica con LEFT JOIN FETCH para traer todo en una sola consulta por hecho
//            Hecho hechoHidratado = em.createQuery(
//                                         "SELECT DISTINCT h FROM Hecho h " +
//                                             "LEFT JOIN FETCH h.colecciones " +
//                                             "LEFT JOIN FETCH h.etiquetas " +
//                                             "LEFT JOIN FETCH h.fotos " +
//                                             "WHERE h.id = :id", Hecho.class)
//                                     .setParameter("id", h.getId())
//                                     .getSingleResult();
//
//            hechosInicializados.add(hechoHidratado);
//          } catch (javax.persistence.NoResultException e) {
//            hechosInicializados.add(h);
//          }
//        } else {
//          hechosInicializados.add(h);
//        }
//      }
//    } catch (Exception e) {
//      System.err.println("Error al inicializar colecciones de hechos: " + e.getMessage());
//      e.printStackTrace();
//      return hechos;
//    } finally {
//      em.close();
//    }
//
//    return hechosInicializados;
//  }
//}