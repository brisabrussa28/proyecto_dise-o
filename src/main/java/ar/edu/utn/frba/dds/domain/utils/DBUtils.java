package ar.edu.utn.frba.dds.domain.utils;

import ar.edu.utn.frba.dds.domain.geolocalizacion.GeoGeoref;
import ar.edu.utn.frba.dds.domain.hecho.Hecho;
import ar.edu.utn.frba.dds.domain.info.PuntoGeografico;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

public class DBUtils {
  // La fábrica ahora no es final y se inicializará de forma "lazy" (perezosa)
  private static EntityManagerFactory factory;

  // Bloque estático para configurar la zona horaria UNA SOLA VEZ al cargar la clase.
  // Esto se ejecuta antes que cualquier método de esta clase sea llamado.
  static {
    TimeZone.setDefault(TimeZone.getTimeZone("America/Argentina/Buenos_Aires"));
  }

  // Método sincronizado para asegurar que la fábrica se cree una sola vez (thread-safe)
  private static synchronized EntityManagerFactory getFactory() {
    if (factory == null) {
      factory = Persistence.createEntityManagerFactory("simple-persistence-unit");
    }
    return factory;
  }

  public static EntityManager getEntityManager() {
    // Ahora usa el método getFactory() para obtener la instancia
    return getFactory().createEntityManager();
  }

  public static void comenzarTransaccion(EntityManager em) {
    EntityTransaction tx = em.getTransaction();
    if (!tx.isActive()) {
      tx.begin();
    }
  }

  public static void commit(EntityManager em) {
    EntityTransaction tx = em.getTransaction();
    if (tx.isActive()) {
      tx.commit();
    }
  }

  public static void rollback(EntityManager em) {
    EntityTransaction tx = em.getTransaction();
    if (tx.isActive()) {
      tx.rollback();
    }
  }

  /**
   * Si la provincia no está definida pero sí la ubicación, intenta obtener la provincia
   * consultando el servicio de geolocalización.
   *
   * @return El propio builder para encadenar llamadas.
   */
  public static void completarProvinciaFaltante(Hecho hecho) {
    if ((hecho.getProvincia() == null || hecho.getProvincia()
                                              .isBlank()) && hecho.getUbicacion() != null) {
      GeoGeoref servicio = new GeoGeoref();
      String provinciaObtenida = servicio.obtenerProvincia(
          hecho.getUbicacion()
               .getLatitud(),
          hecho.getUbicacion()
               .getLongitud()
      );
      if (provinciaObtenida != null && !provinciaObtenida.isBlank()) {
        hecho.setProvincia(provinciaObtenida); // CORRECCIÓN: Asignar la provincia encontrada
      }
    }
  }

  /**
   * Si la ubicación no está definida pero sí la provincia, intenta obtener la ubicación
   * consultando el servicio de geolocalización.
   *
   * @return El propio builder para encadenar llamadas.
   */
  public static void completarUbicacionFaltante(Hecho hecho) {
    if (hecho.getUbicacion() == null && hecho.getProvincia() != null && !hecho.getProvincia()
                                                                              .isBlank()) {
      GeoGeoref servicio = new GeoGeoref();
      CompletableFuture<PuntoGeografico> ubicacionObtenida = servicio.obtenerUbicacion(hecho.getProvincia());
      if (ubicacionObtenida != null) {
        hecho.setUbicacion(ubicacionObtenida);
      }
    }
  }
}
