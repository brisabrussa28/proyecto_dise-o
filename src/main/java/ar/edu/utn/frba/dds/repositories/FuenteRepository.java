package ar.edu.utn.frba.dds.repositories;

import ar.edu.utn.frba.dds.model.fuentes.Fuente;
import ar.edu.utn.frba.dds.utils.DBUtils;
import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;
import java.util.List;

public class FuenteRepository implements WithSimplePersistenceUnit {
  private static final FuenteRepository INSTANCE = new FuenteRepository();

  public static FuenteRepository instance() {
    return INSTANCE;
  }

  public void save(Fuente fuente) {
    fuente.getHechos()
          .forEach(hecho -> {
            DBUtils.completarUbicacionFaltante(hecho);
            DBUtils.completarProvinciaFaltante(hecho);
          });
    entityManager().persist(fuente);
  }

  public List<Fuente> findAll() {
    return entityManager().createQuery("SELECT * FROM Fuente", Fuente.class)
                          .getResultList();

  }

  public Fuente findById(Long id) {
    return entityManager().find(Fuente.class, id);
  }

}
