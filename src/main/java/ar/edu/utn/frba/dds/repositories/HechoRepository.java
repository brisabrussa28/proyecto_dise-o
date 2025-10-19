package ar.edu.utn.frba.dds.repositories;

import ar.edu.utn.frba.dds.model.hecho.Hecho;
// Eliminamos DBUtils, ya no se usa aquí
import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;
import java.util.List;

public class HechoRepository implements WithSimplePersistenceUnit {
  private static final HechoRepository INSTANCE = new HechoRepository();

  public static HechoRepository instance() {
    return INSTANCE;
  }

  public void save(Hecho hecho) {
    if (hecho.getId() == null) {
      entityManager().persist(hecho);
    } else {
      entityManager().merge(hecho);
    }
  }

  public List<Hecho> findAll() {
    return entityManager().createQuery("SELECT h FROM Hecho h", Hecho.class)
                          .getResultList();
  }

  public Hecho getById(Long id) {
    return entityManager().find(Hecho.class, id);
  }
}