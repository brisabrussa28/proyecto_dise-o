package ar.edu.utn.frba.dds.domain.repos;

import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.reportes.EstadoSolicitud;
import ar.edu.utn.frba.dds.domain.reportes.Solicitud;
import ar.edu.utn.frba.dds.domain.utils.DBUtils;
import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

public class RepositorioDeSolicitudes {
  private final EntityManager em = DBUtils.getEntityManager();

  public RepositorioDeSolicitudes() {
  }

  public void guardar(Solicitud solicitud) {
    if (solicitud == null) return;
    DBUtils.comenzarTransaccion(em);
    em.persist(solicitud);
    DBUtils.commit(em);
  }

  public Optional<Solicitud> buscarPorId(Long id) {
    return Optional.ofNullable(em.find(Solicitud.class, id));
  }

  public Optional<Solicitud> buscarPorHechoYRazon(Hecho hecho, String razon) {
    try {
      Solicitud solicitud = em.createQuery(
                                  "SELECT s FROM Solicitud s WHERE s.hechoSolicitado = :hecho AND s.razonEliminacion = :razon", Solicitud.class)
                              .setParameter("hecho", hecho)
                              .setParameter("razon", razon)
                              .getSingleResult();
      return Optional.of(solicitud);
    } catch (NoResultException e) {
      return Optional.empty();
    }
  }

  public List<Solicitud> obtenerTodas() {
    return em.createQuery("SELECT s FROM Solicitud s", Solicitud.class).getResultList();
  }

  public List<Solicitud> obtenerPorEstado(EstadoSolicitud estado) {
    return em.createQuery("SELECT s FROM Solicitud s WHERE s.estado = :estado", Solicitud.class)
             .setParameter("estado", estado)
             .getResultList();
  }

  public long cantidadTotal() {
    return em.createQuery("SELECT COUNT(s) FROM Solicitud s", Long.class).getSingleResult();
  }
}
