package ar.edu.utn.frba.dds.repositories;

import ar.edu.utn.frba.dds.model.coleccion.Coleccion;
import ar.edu.utn.frba.dds.utils.DBUtils;
import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;
import java.util.List;

public class ColeccionRepository implements WithSimplePersistenceUnit {
  private static final ColeccionRepository INSTANCE = new ColeccionRepository();

  public static ColeccionRepository instance() {
    return INSTANCE;
  }

  public void save(Coleccion coleccion) {
    coleccion.getHechosConsensuados()
             .forEach(hecho -> {
               DBUtils.enriquecerHecho(hecho);
             });
    entityManager().persist(coleccion);
  }

  public List<Coleccion> findAll() {
    return entityManager().createQuery("select c from Coleccion c", Coleccion.class)
                          .getResultList();

  }

  public Coleccion findById(Long id) {
    return entityManager().find(Coleccion.class, id);
  }
}