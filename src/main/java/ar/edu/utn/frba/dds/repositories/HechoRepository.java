package ar.edu.utn.frba.dds.repositories;

import ar.edu.utn.frba.dds.model.hecho.Hecho;
import ar.edu.utn.frba.dds.utils.DBUtils;
import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;
import java.util.List;
import java.util.Optional;

public class HechoRepository implements WithSimplePersistenceUnit {
  private static final HechoRepository INSTANCE = new HechoRepository();

  public static HechoRepository instance() {
    return INSTANCE;
  }

  public void save(Hecho hecho) {
    DBUtils.completarUbicacionFaltante(hecho);
    DBUtils.completarProvinciaFaltante(hecho);
    entityManager().persist(hecho);
  }

  public List<Hecho> findAll() {
    return entityManager().createQuery("SELECT * FROM Hecho", Hecho.class)
                          .getResultList();
  }

  public Optional<Hecho> findAny() {
    return this.findAll().stream().findAny();
  }

  public Hecho getById(Long id) {
    return entityManager().find(Hecho.class, id);
  }
}
