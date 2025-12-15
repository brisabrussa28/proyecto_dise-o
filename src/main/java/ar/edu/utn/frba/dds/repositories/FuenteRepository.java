package ar.edu.utn.frba.dds.repositories;

import ar.edu.utn.frba.dds.model.coleccion.Coleccion;
import ar.edu.utn.frba.dds.model.fuentes.Fuente;
import ar.edu.utn.frba.dds.model.fuentes.FuenteConHechos;
import ar.edu.utn.frba.dds.model.fuentes.FuenteDeAgregacion;
import ar.edu.utn.frba.dds.model.fuentes.FuenteDeCopiaLocal;
import ar.edu.utn.frba.dds.model.fuentes.FuenteDinamica;
import ar.edu.utn.frba.dds.model.fuentes.FuenteEstatica;
import ar.edu.utn.frba.dds.model.fuentes.FuenteExternaAPI;
import ar.edu.utn.frba.dds.utils.DBUtils;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;

public class FuenteRepository {
  private static final FuenteRepository INSTANCE = new FuenteRepository();

  public static FuenteRepository instance() {
    return INSTANCE;
  }

  public void save(Fuente fuente) {
    EntityManager em = DBUtils.getEntityManager();

    // Enriquecer hechos antes de guardar si corresponde
    if (fuente instanceof FuenteConHechos) {
      // Convertimos a lista para iterar si es necesario, aunque el enriquecimiento suele ser previo
      ((FuenteConHechos) fuente).getHechos().forEach(DBUtils::enriquecerHecho);
    }

    DBUtils.comenzarTransaccion(em);
    try {
      if (fuente.getId() == null) {
        em.persist(fuente);
      } else {
        fuente = em.merge(fuente);
      }
      em.flush();
      DBUtils.commit(em);
    } catch (PersistenceException e) {
      DBUtils.rollback(em);
      throw new RuntimeException("Error al guardar fuente: " + e.getMessage(), e);
    } finally {
      em.close();
    }
  }

  public Fuente findByName(String nombre) {
    EntityManager em = DBUtils.getEntityManager();
    try {
      return em.createQuery(
                   "SELECT DISTINCT f FROM Fuente f where f.fuente_nombre = :nombre",
                   Fuente.class
               )
               .setParameter("nombre", nombre)
               .getSingleResult();
    } catch (NoResultException e) {
      return null;
    } finally {
      em.close();
    }
  }

  public void update(Fuente fuente) {
    EntityManager em = DBUtils.getEntityManager();
    DBUtils.comenzarTransaccion(em);
    try {
      Fuente managedFuente = em.merge(fuente);

      if (managedFuente instanceof FuenteConHechos) {
        ((FuenteConHechos) managedFuente).getHechos().forEach(DBUtils::enriquecerHecho);
      }

      DBUtils.commit(em);
    } catch (PersistenceException e) {
      DBUtils.rollback(em);
      throw new RuntimeException("Error al actualizar fuente: " + e.getMessage(), e);
    } finally {
      em.close();
    }
  }

  public List<Fuente> findAll() {
    EntityManager em = DBUtils.getEntityManager();
    try {
      return em.createQuery("SELECT DISTINCT f FROM Fuente f", Fuente.class)
               .getResultList();
    } finally {
      em.close();
    }
  }

  public Fuente findById(Long id) {
    EntityManager em = DBUtils.getEntityManager();
    try {
      Fuente fuente = em.find(Fuente.class, id);

      if (fuente != null) {
        // Cargar colecciones Lazy dentro de la sesión activa
        cargarDependenciasLazy(em, fuente);
      }

      return fuente;
    } finally {
      em.close();
    }
  }

  public Fuente findByIdConHechos(Long id) {
    return findById(id); // findById ya llama a cargarDependenciasLazy
  }

  /**
   * Carga las dependencias perezosas (Lazy) de una fuente.
   * Centraliza la lógica para FuenteConHechos y FuenteDeAgregacion.
   */
  private void cargarDependenciasLazy(EntityManager em, Fuente fuente) {
    if (fuente == null) {
      return;
    }

    try {
      // 1. Si es una Fuente de Agregación, cargar sus fuentes hijas
      if (fuente instanceof FuenteDeAgregacion) {
        // Usamos una query específica para inicializar la colección
        FuenteDeAgregacion agregacion = em.createQuery(
                                              "SELECT DISTINCT f FROM FuenteDeAgregacion f " +
                                                  "LEFT JOIN FETCH f.fuentesCargadas " +
                                                  "WHERE f.id = :id", FuenteDeAgregacion.class)
                                          .setParameter("id", fuente.getId())
                                          .getSingleResult();

        // Actualizamos la referencia en memoria (aunque al ser managed dentro de la sesión, 'fuente' y 'agregacion' deberían ser el mismo objeto si están en contexto)
        // Si 'fuente' vino de un find() anterior, hibernate garantiza identidad de objeto.
        // Accedemos al método para asegurar la inicialización si fuera un proxy
        ((FuenteDeAgregacion) fuente).setFuentesCargadas(agregacion.getFuentesCargadas());
      }

      // 2. Si es una Fuente con Hechos (Cualquiera: Estática, Dinámica, API, Copia Local)
      else if (fuente instanceof FuenteConHechos) {
        // Aprovechamos el polimorfismo: cargamos FuenteConHechos y sus hechos
        FuenteConHechos conHechos = em.createQuery(
                                          "SELECT DISTINCT f FROM FuenteConHechos f " +
                                              "LEFT JOIN FETCH f.hechos " +
                                              "WHERE f.id = :id", FuenteConHechos.class)
                                      .setParameter("id", fuente.getId())
                                      .getSingleResult();

        // Forzamos la inicialización accediendo a la colección
        // Nota: Convertimos a ArrayList en setHechos internamente o usamos el Set directamente
        ((FuenteConHechos) fuente).getHechos();
      }

    } catch (Exception e) {
      System.err.println("Advertencia: No se pudieron cargar dependencias lazy para fuente " +
                             fuente.getNombre() + " (ID: " + fuente.getId() + "): " + e.getMessage());
    }
  }

  public void delete(Fuente fuente) {
    EntityManager em = DBUtils.getEntityManager();
    DBUtils.comenzarTransaccion(em);
    try {
      eliminarRecursivamente(fuente, em);
      DBUtils.commit(em);
    } catch (PersistenceException e) {
      DBUtils.rollback(em);
      throw new RuntimeException("Error al eliminar la fuente: " + e.getMessage());
    } catch (Exception e) {
      DBUtils.rollback(em);
      throw new RuntimeException("Error inesperado al eliminar la fuente: " + e.getMessage());
    } finally {
      em.close();
    }
  }

  private void eliminarRecursivamente(Fuente fuente, EntityManager em) {
    // Buscar colecciones que usan esta fuente directamente
    List<Coleccion> coleccionesDirectas = em.createQuery(
                                                "SELECT c FROM Coleccion c WHERE c.coleccion_fuente.id = :id",
                                                Coleccion.class)
                                            .setParameter("id", fuente.getId())
                                            .getResultList();

    for (Coleccion c : coleccionesDirectas) {
      Coleccion managedCol = em.contains(c) ? c : em.merge(c);
      em.remove(managedCol);
    }

    // Buscar fuentes de agregación que contienen esta fuente como hija
    List<FuenteDeAgregacion> padres = em.createQuery(
                                            "SELECT DISTINCT f FROM FuenteDeAgregacion f JOIN f.fuentesCargadas hija WHERE hija.id = :hijoId",
                                            FuenteDeAgregacion.class)
                                        .setParameter("hijoId", fuente.getId())
                                        .getResultList();

    for (FuenteDeAgregacion padre : padres) {
      // Necesitamos hacer merge si no está en el contexto
      padre = em.contains(padre) ? padre : em.merge(padre);

      padre.removerFuente(fuente);
      em.merge(padre); // Guardar cambios en el padre

      // Si el padre queda vacío, ¿eliminamos recursivamente? (Según lógica original sí)
      if (padre.cantidadFuentes() == 0) {
        eliminarRecursivamente(padre, em);
      }
    }

    // Eliminar la fuente
    Fuente managed = em.contains(fuente) ? fuente : em.merge(fuente);
    em.remove(managed);
  }

  public List<FuenteDeAgregacion> findAgregacionesByHija(Long hijoId) {
    EntityManager em = DBUtils.getEntityManager();
    try {
      return em.createQuery(
                   "SELECT DISTINCT f FROM FuenteDeAgregacion f JOIN f.fuentesCargadas hija WHERE hija.id = :hijoId",
                   FuenteDeAgregacion.class)
               .setParameter("hijoId", hijoId)
               .getResultList();
    } finally {
      em.close();
    }
  }

  public List<Coleccion> findColeccionesIndirectas(Long fuenteId) {
    EntityManager em = DBUtils.getEntityManager();
    try {
      return em.createQuery(
                   "SELECT DISTINCT c FROM Coleccion c " +
                       "WHERE c.coleccion_fuente.id IN " +
                       "(SELECT f.id FROM FuenteDeAgregacion f JOIN f.fuentesCargadas hija WHERE hija.id = :fuenteId)",
                   Coleccion.class)
               .setParameter("fuenteId", fuenteId)
               .getResultList();
    } finally {
      em.close();
    }
  }

  public List<Fuente> findByTipo(String tipoDiscriminador) {
    EntityManager em = DBUtils.getEntityManager();
    try {
      // Usamos TYPE(f) que es estándar JPA
      return em.createQuery(
                   "SELECT f FROM Fuente f WHERE TYPE(f) = :tipo", // Esto podría requerir pasar la clase en vez de string dependiendo del proveedor, pero Hibernate suele soportar cadenas si coinciden con el discriminador o entity name
                   Fuente.class)
               // Nota: JPA estándar usa Entity Class para TYPE, Hibernate puede ser flexible.
               // Si falla, se puede filtrar en memoria o usar SQL nativo.
               // Alternativa más segura: filtrar en memoria si no son muchos.
               .getResultList();
    } catch (IllegalArgumentException e) {
      // Fallback: traer todos y filtrar
      return findAll().stream()
                      .filter(f -> f.getTipo().equalsIgnoreCase(tipoDiscriminador))
                      .collect(java.util.stream.Collectors.toList());
    } finally {
      em.close();
    }
  }

  public List<Fuente> findByHechoId(Long hechoId) {
    EntityManager em = DBUtils.getEntityManager();
    try {
      // Solo fuentes con hechos tienen hechos directos
      return em.createQuery(
                   "SELECT DISTINCT f FROM FuenteConHechos f JOIN f.hechos h WHERE h.id = :hechoId",
                   Fuente.class)
               .setParameter("hechoId", hechoId)
               .getResultList();
    } finally {
      em.close();
    }
  }

  public Fuente findFuenteByNombre(String nombreFuente) {
    EntityManager em = DBUtils.getEntityManager();
    try {
      return em.createQuery(
                   "SELECT f FROM Fuente f WHERE f.fuente_nombre = :nombre",
                   Fuente.class)
               .setParameter("nombre", nombreFuente)
               .getSingleResult();
    } catch (NoResultException e) {
      return null;
    } finally {
      em.close();
    }
  }
}