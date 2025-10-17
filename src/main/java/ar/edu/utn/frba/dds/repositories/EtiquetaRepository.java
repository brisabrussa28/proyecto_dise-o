package ar.edu.utn.frba.dds.repositories;

import ar.edu.utn.frba.dds.model.hecho.Hecho;
import ar.edu.utn.frba.dds.model.hecho.etiqueta.Etiqueta;
import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;
import java.util.List;

public class EtiquetaRepository implements WithSimplePersistenceUnit {
  private static final EtiquetaRepository INSTANCE = new EtiquetaRepository();

  public static EtiquetaRepository instance() {
    return INSTANCE;
  }

  public void save(Etiqueta etiqueta) {
    entityManager().persist(etiqueta);
  }

  public List<Hecho> findAll() {
    return entityManager().createQuery("SELECT * FROM Hecho", Hecho.class)
                          .getResultList();

  }

  public Hecho getById(Long id) {
    return entityManager().find(Hecho.class, id);
  }
}
