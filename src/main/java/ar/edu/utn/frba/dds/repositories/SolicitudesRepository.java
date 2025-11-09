package ar.edu.utn.frba.dds.repositories;

import ar.edu.utn.frba.dds.model.hecho.Hecho;
import ar.edu.utn.frba.dds.model.reportes.EstadoSolicitud;
import ar.edu.utn.frba.dds.model.reportes.Solicitud;
import ar.edu.utn.frba.dds.utils.DBUtils;
import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

/**
 * Repositorio de Solicitudes. Responsable únicamente del almacenamiento
 * y recuperación de las solicitudes desde la base de datos.
 */
public class SolicitudesRepository {
  private final EntityManager em = DBUtils.getEntityManager();

  private static final SolicitudesRepository INSTANCE = new SolicitudesRepository();

  public static SolicitudesRepository instance() {
    return INSTANCE;
  }

  /**
   * Guarda o actualiza una solicitud en la base de datos.
   * Si la solicitud ya tiene un ID, la actualiza (merge).
   * Si es nueva (sin ID), la inserta (persist).
   *
   * @param solicitud La entidad Solicitud a persistir.
   */
  public void guardar(Solicitud solicitud) {
    DBUtils.comenzarTransaccion(em);
    if (solicitud.id == null) {
      em.persist(solicitud);
    } else {
      em.merge(solicitud);
    }
    DBUtils.commit(em);
  }

  /**
   * Busca una solicitud específica por su ID.
   *
   * @param id El ID de la solicitud.
   * @return Un Optional con la solicitud si se encuentra, o vacío si no.
   */
  public Solicitud findById(Long id) {
    return em.find(Solicitud.class, id);
  }

  /**
   * Busca una solicitud basada en el hecho solicitado y el motivo.
   *
   * @param hecho El hecho asociado a la solicitud.
   * @param razon El texto del motivo de la solicitud.
   * @return Un Optional con la solicitud si se encuentra.
   */
  public Optional<Solicitud> buscarPorHechoYRazon(Hecho hecho, String razon) {
    try {
      Solicitud solicitud = em.createQuery(
                                  "SELECT s FROM Solicitud s WHERE s.hechoSolicitado = :hecho AND s.razonEliminacion = :razon",
                                  Solicitud.class
                              )
                              .setParameter("hecho", hecho)
                              .setParameter("razon", razon)
                              .getSingleResult();
      return Optional.of(solicitud);
    } catch (NoResultException e) {
      return Optional.empty();
    }
  }

  /**
   * Devuelve todas las solicitudes almacenadas en la base de datos.
   *
   * @return Una lista con todas las solicitudes.
   */
  public List<Solicitud> findAll() {
    return em.createQuery("SELECT s FROM Solicitud s", Solicitud.class)
             .getResultList();
  }

  /**
   * Devuelve todas las solicitudes que se encuentran en un estado específico.
   *
   * @param estado El estado por el cual filtrar (PENDIENTE, SPAM, etc.).
   * @return Una lista de solicitudes que coinciden con el estado.
   */
  public List<Solicitud> obtenerPorEstado(EstadoSolicitud estado) {
    return em.createQuery(
                 "SELECT s FROM Solicitud s WHERE s.estado = :estado",
                 Solicitud.class
             )
             .setParameter("estado", estado)
             .getResultList();
  }

  /**
   * Cuenta el número total de solicitudes en la base de datos.
   *
   * @return La cantidad total de solicitudes.
   */
  public int cantidadTotal() {
    Long count = em.createQuery("SELECT COUNT(s) FROM Solicitud s", Long.class)
                   .getSingleResult();
    return count.intValue();
  }

  public void aceptarSolicitud(Solicitud solicitud) {
    DBUtils.comenzarTransaccion(em);
    solicitud.aceptar();
    em.merge(solicitud);
    DBUtils.commit(em);
  }

  public void rechazarSolicitud(Solicitud solicitud) {
    DBUtils.comenzarTransaccion(em);
    solicitud.rechazar();
    em.merge(solicitud);
    DBUtils.commit(em);
  }
}
